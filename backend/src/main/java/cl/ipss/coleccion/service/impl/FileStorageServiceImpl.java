package cl.ipss.coleccion.service.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import cl.ipss.coleccion.exception.ArchivoInvalidoException;
import cl.ipss.coleccion.exception.RecursoNoEncontradoException;
import cl.ipss.coleccion.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementacion de {@link FileStorageService} sobre el sistema de archivos local
 * ({@code app.upload.dir}, montado como volumen en Docker).
 * <p>
 * Seguridad: el tipo se detecta por los primeros bytes del archivo (no por la extension ni
 * por el Content-Type que declara el cliente), los nombres se generan con UUID y toda ruta se
 * normaliza y verifica dentro del directorio de subidas (evita path traversal).
 */
@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

	private static final Pattern NOMBRE_VALIDO = Pattern.compile("^[0-9a-f-]{36}\\.(jpg|png|webp)$");
	private static final byte[] FIRMA_PNG = { (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n' };

	private final Path directorio;

	/**
	 * @param directorio ruta del directorio de subidas; se crea si no existe
	 * @throws IOException si no se puede crear el directorio
	 */
	public FileStorageServiceImpl(@Value("${app.upload.dir}") String directorio) throws IOException {
		this.directorio = Path.of(directorio).toAbsolutePath().normalize();
		Files.createDirectories(this.directorio);
		log.info("Directorio de fotos: {}", this.directorio);
	}

	@Override
	public String guardar(MultipartFile archivo) {
		if (archivo == null || archivo.isEmpty()) {
			throw new ArchivoInvalidoException("El archivo está vacío");
		}
		try {
			byte[] contenido = archivo.getBytes();
			String extension = detectarExtension(contenido).orElseThrow(
					() -> new ArchivoInvalidoException("Solo se permiten imágenes JPEG, PNG o WEBP"));
			String nombre = UUID.randomUUID() + "." + extension;
			Files.write(resolver(nombre), contenido, StandardOpenOption.CREATE_NEW);
			programarEliminacion(nombre, false); // se borra si la transaccion hace rollback
			return nombre;
		} catch (IOException e) {
			throw new UncheckedIOException("No se pudo guardar la foto", e);
		}
	}

	@Override
	public Resource cargar(String nombre) {
		if (!NOMBRE_VALIDO.matcher(nombre).matches() || !Files.isRegularFile(resolver(nombre))) {
			throw new RecursoNoEncontradoException("El archivo de la foto no existe");
		}
		return new FileSystemResource(resolver(nombre));
	}

	@Override
	public String tipoContenido(String nombre) {
		String extension = nombre.substring(nombre.lastIndexOf('.') + 1);
		return switch (extension) {
			case "png" -> "image/png";
			case "webp" -> "image/webp";
			default -> "image/jpeg";
		};
	}

	@Override
	public void eliminarAlConfirmar(String nombre) {
		programarEliminacion(nombre, true);
	}

	/**
	 * Borra el archivo al terminar la transaccion: tras un commit si {@code alConfirmar} es
	 * verdadero, o tras un rollback si es falso. Sin transaccion activa, borra de inmediato
	 * solo en el caso de confirmacion.
	 */
	private void programarEliminacion(String nombre, boolean alConfirmar) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			if (alConfirmar) {
				borrar(nombre);
			}
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCompletion(int estado) {
				if ((estado == STATUS_COMMITTED) == alConfirmar) {
					borrar(nombre);
				}
			}
		});
	}

	private void borrar(String nombre) {
		try {
			if (Files.deleteIfExists(resolver(nombre))) {
				log.info("Foto eliminada del disco: {}", nombre);
			}
		} catch (IOException e) {
			log.warn("No se pudo eliminar la foto {}: {}", nombre, e.getMessage());
		}
	}

	private Path resolver(String nombre) {
		Path ruta = directorio.resolve(nombre).normalize();
		if (!ruta.startsWith(directorio)) {
			throw new ArchivoInvalidoException("Nombre de archivo inválido");
		}
		return ruta;
	}

	// Firmas: JPEG FF D8 FF | PNG 89 50 4E 47 0D 0A 1A 0A | WEBP "RIFF" ???? "WEBP"
	private static Optional<String> detectarExtension(byte[] b) {
		if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
			return Optional.of("jpg");
		}
		if (b.length >= 8 && Arrays.equals(Arrays.copyOf(b, 8), FIRMA_PNG)) {
			return Optional.of("png");
		}
		if (b.length >= 12 && "RIFF".equals(new String(b, 0, 4, StandardCharsets.US_ASCII))
				&& "WEBP".equals(new String(b, 8, 4, StandardCharsets.US_ASCII))) {
			return Optional.of("webp");
		}
		return Optional.empty();
	}
}
