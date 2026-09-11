package cl.ipss.coleccion.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import cl.ipss.coleccion.dto.ArchivoFoto;
import cl.ipss.coleccion.dto.LaminaRequest;
import cl.ipss.coleccion.dto.LaminaResponse;
import cl.ipss.coleccion.entity.Album;
import cl.ipss.coleccion.entity.Lamina;
import cl.ipss.coleccion.entity.TipoLamina;
import cl.ipss.coleccion.exception.RecursoNoEncontradoException;
import cl.ipss.coleccion.exception.ReglaNegocioException;
import cl.ipss.coleccion.mapper.LaminaMapper;
import cl.ipss.coleccion.repository.AlbumRepository;
import cl.ipss.coleccion.repository.LaminaRepository;
import cl.ipss.coleccion.service.FileStorageService;
import cl.ipss.coleccion.service.LaminaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementacion de {@link LaminaService}. Valida que el numero de cada lamina este dentro
 * del rango del album y no se repita dentro de el.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LaminaServiceImpl implements LaminaService {

	private final LaminaRepository laminaRepository;
	private final AlbumRepository albumRepository;
	private final FileStorageService fileStorageService;

	@Override
	@Transactional
	public LaminaResponse crear(Long albumId, LaminaRequest request) {
		Album album = buscarAlbum(albumId);
		validarNumero(album, request.numero(), null);
		Lamina lamina = laminaRepository.save(LaminaMapper.toEntity(request, album));
		log.info("Lamina creada id={} albumId={} numero={}", lamina.getId(), albumId, lamina.getNumero());
		return LaminaMapper.toResponse(lamina);
	}

	@Override
	public List<LaminaResponse> listarPorAlbum(Long albumId, TipoLamina tipo) {
		if (!albumRepository.existsById(albumId)) {
			throw albumNoEncontrado(albumId);
		}
		List<Lamina> laminas = tipo == null
				? laminaRepository.findByAlbumIdOrderByNumeroAsc(albumId)
				: laminaRepository.findByAlbumIdAndTipoOrderByNumeroAsc(albumId, tipo);
		return laminas.stream().map(LaminaMapper::toResponse).toList();
	}

	@Override
	public LaminaResponse obtener(Long id) {
		return LaminaMapper.toResponse(buscar(id));
	}

	@Override
	@Transactional
	public LaminaResponse actualizar(Long id, LaminaRequest request) {
		Lamina lamina = buscar(id);
		validarNumero(lamina.getAlbum(), request.numero(), id);
		LaminaMapper.actualizar(lamina, request);
		laminaRepository.saveAndFlush(lamina); // aplica la auditoria (modificadoEn/Por) antes de responder
		log.info("Lamina actualizada id={}", id);
		return LaminaMapper.toResponse(lamina);
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		Lamina lamina = buscar(id);
		laminaRepository.delete(lamina);
		if (lamina.getFoto() != null) {
			fileStorageService.eliminarAlConfirmar(lamina.getFoto());
		}
		log.info("Lamina eliminada id={}", id);
	}

	@Override
	@Transactional
	public LaminaResponse subirFoto(Long id, MultipartFile archivo) {
		Lamina lamina = buscar(id);
		String anterior = lamina.getFoto();
		lamina.setFoto(fileStorageService.guardar(archivo));
		if (anterior != null) {
			fileStorageService.eliminarAlConfirmar(anterior);
		}
		laminaRepository.saveAndFlush(lamina);
		log.info("Foto {} laminaId={} archivo={}", anterior == null ? "subida" : "reemplazada", id, lamina.getFoto());
		return LaminaMapper.toResponse(lamina);
	}

	@Override
	public ArchivoFoto obtenerFoto(Long id) {
		String foto = fotoDe(buscar(id));
		return new ArchivoFoto(fileStorageService.cargar(foto), fileStorageService.tipoContenido(foto));
	}

	@Override
	@Transactional
	public void eliminarFoto(Long id) {
		Lamina lamina = buscar(id);
		fileStorageService.eliminarAlConfirmar(fotoDe(lamina));
		lamina.setFoto(null);
		log.info("Foto eliminada laminaId={}", id);
	}

	private static String fotoDe(Lamina lamina) {
		if (lamina.getFoto() == null) {
			throw new RecursoNoEncontradoException("La lámina con id " + lamina.getId() + " no tiene foto");
		}
		return lamina.getFoto();
	}

	private Lamina buscar(Long id) {
		return laminaRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe una lámina con id " + id));
	}

	private Album buscarAlbum(Long albumId) {
		return albumRepository.findById(albumId).orElseThrow(() -> albumNoEncontrado(albumId));
	}

	private static RecursoNoEncontradoException albumNoEncontrado(Long albumId) {
		return new RecursoNoEncontradoException("No existe un álbum con id " + albumId);
	}

	private void validarNumero(Album album, int numero, Long idActual) {
		if (numero > album.getTotalLaminas()) {
			throw new ReglaNegocioException("El número %d excede el total de láminas del álbum (%d)"
					.formatted(numero, album.getTotalLaminas()));
		}
		boolean duplicado = idActual == null
				? laminaRepository.existsByAlbumIdAndNumero(album.getId(), numero)
				: laminaRepository.existsByAlbumIdAndNumeroAndIdNot(album.getId(), numero, idActual);
		if (duplicado) {
			throw new ReglaNegocioException("El álbum ya tiene una lámina con el número " + numero);
		}
	}
}
