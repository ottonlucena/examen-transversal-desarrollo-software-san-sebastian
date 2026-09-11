package cl.ipss.coleccion.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import cl.ipss.coleccion.exception.ArchivoInvalidoException;
import cl.ipss.coleccion.exception.RecursoNoEncontradoException;

/**
 * Almacenamiento de las fotos de laminas en disco. En la base de datos solo se guarda el
 * nombre del archivo; el contenido nunca se guarda como Base64 ni BLOB.
 */
public interface FileStorageService {

	/**
	 * Valida y guarda una imagen con un nombre unico. Si hay una transaccion activa y termina
	 * en rollback, el archivo se elimina para no dejar huerfanos.
	 *
	 * @param archivo imagen recibida por multipart
	 * @return nombre generado del archivo (UUID + extension)
	 * @throws ArchivoInvalidoException si esta vacio o no es JPEG, PNG o WEBP (segun su contenido real)
	 */
	String guardar(MultipartFile archivo);

	/**
	 * @param nombre nombre devuelto por {@link #guardar(MultipartFile)}
	 * @return archivo en disco
	 * @throws RecursoNoEncontradoException si el archivo no existe
	 */
	Resource cargar(String nombre);

	/**
	 * @param nombre nombre del archivo
	 * @return tipo MIME segun la extension
	 */
	String tipoContenido(String nombre);

	/**
	 * Elimina el archivo cuando la transaccion actual se confirme, o de inmediato si no hay
	 * transaccion. Asi, un rollback no deja registros apuntando a archivos borrados.
	 *
	 * @param nombre nombre del archivo
	 */
	void eliminarAlConfirmar(String nombre);
}
