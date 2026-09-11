package cl.ipss.coleccion.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.ipss.coleccion.dto.AlbumRequest;
import cl.ipss.coleccion.dto.AlbumResponse;
import cl.ipss.coleccion.entity.Album;
import cl.ipss.coleccion.entity.Lamina;
import cl.ipss.coleccion.exception.RecursoNoEncontradoException;
import cl.ipss.coleccion.exception.ReglaNegocioException;
import cl.ipss.coleccion.mapper.AlbumMapper;
import cl.ipss.coleccion.repository.AlbumRepository;
import cl.ipss.coleccion.repository.LaminaRepository;
import cl.ipss.coleccion.service.AlbumService;
import cl.ipss.coleccion.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementacion de {@link AlbumService}. Las lecturas son transaccionales de solo lectura
 * y cada escritura abre su propia transaccion.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlbumServiceImpl implements AlbumService {

	private final AlbumRepository albumRepository;
	private final LaminaRepository laminaRepository;
	private final FileStorageService fileStorageService;

	@Override
	@Transactional
	public AlbumResponse crear(AlbumRequest request) {
		validarNombreUnico(request.nombre().strip(), null);
		Album album = albumRepository.save(AlbumMapper.toEntity(request));
		log.info("Album creado id={} nombre='{}'", album.getId(), album.getNombre());
		return AlbumMapper.toResponse(album);
	}

	@Override
	public Page<AlbumResponse> listar(Pageable pageable) {
		return albumRepository.findAll(pageable).map(AlbumMapper::toResponse);
	}

	@Override
	public AlbumResponse obtener(Long id) {
		return AlbumMapper.toResponse(buscar(id));
	}

	@Override
	@Transactional
	public AlbumResponse actualizar(Long id, AlbumRequest request) {
		Album album = buscar(id);
		validarNombreUnico(request.nombre().strip(), id);
		validarTotalLaminas(id, request.totalLaminas());
		AlbumMapper.actualizar(album, request);
		albumRepository.saveAndFlush(album); // aplica la auditoria (modificadoEn/Por) antes de responder
		log.info("Album actualizado id={}", id);
		return AlbumMapper.toResponse(album);
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		Album album = buscar(id);
		// Se eliminan las laminas como entidades (no solo por la cascada de la BD) para que
		// Envers registre la eliminacion de cada una en la auditoria
		List<Lamina> laminas = laminaRepository.findByAlbumIdOrderByNumeroAsc(id);
		laminaRepository.deleteAll(laminas);
		albumRepository.delete(album);
		laminas.stream().map(Lamina::getFoto).filter(Objects::nonNull)
				.forEach(fileStorageService::eliminarAlConfirmar);
		log.info("Album eliminado id={} junto a {} lamina(s)", id, laminas.size());
	}

	private Album buscar(Long id) {
		return albumRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe un álbum con id " + id));
	}

	private void validarNombreUnico(String nombre, Long idActual) {
		boolean existe = idActual == null
				? albumRepository.existsByNombreIgnoreCase(nombre)
				: albumRepository.existsByNombreIgnoreCaseAndIdNot(nombre, idActual);
		if (existe) {
			throw new ReglaNegocioException("Ya existe un álbum con el nombre '" + nombre + "'");
		}
	}

	// Impide reducir el total por debajo de una lamina ya registrada (quedaria fuera de rango)
	private void validarTotalLaminas(Long albumId, int nuevoTotal) {
		laminaRepository.findMaxNumeroByAlbumId(albumId)
				.filter(maximo -> maximo > nuevoTotal)
				.ifPresent(maximo -> {
					throw new ReglaNegocioException(
							"No se puede reducir el total a %d: el álbum tiene registrada la lámina número %d"
									.formatted(nuevoTotal, maximo));
				});
	}
}
