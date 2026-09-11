package cl.ipss.coleccion.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Function;

import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.ipss.coleccion.dto.AlbumResponse;
import cl.ipss.coleccion.dto.LaminaResponse;
import cl.ipss.coleccion.dto.RegistroHistorial;
import cl.ipss.coleccion.dto.TipoOperacion;
import cl.ipss.coleccion.entity.Album;
import cl.ipss.coleccion.entity.Lamina;
import cl.ipss.coleccion.entity.Revision;
import cl.ipss.coleccion.exception.RecursoNoEncontradoException;
import cl.ipss.coleccion.mapper.AlbumMapper;
import cl.ipss.coleccion.mapper.LaminaMapper;
import cl.ipss.coleccion.service.HistorialService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * Implementacion de {@link HistorialService} con el {@code AuditReader} de Hibernate Envers.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistorialServiceImpl implements HistorialService {

	private static final ZoneId ZONA = ZoneId.of("America/Santiago");

	private final EntityManager entityManager;

	@Override
	public List<RegistroHistorial<AlbumResponse>> historialAlbum(Long albumId) {
		return consultar(Album.class, albumId, "el álbum", AlbumMapper::toResponse);
	}

	@Override
	public List<RegistroHistorial<LaminaResponse>> historialLamina(Long laminaId) {
		return consultar(Lamina.class, laminaId, "la lámina", LaminaMapper::toResponse);
	}

	/**
	 * Obtiene todas las revisiones de una entidad. Cada fila de Envers es
	 * {@code [entidad en esa revision, Revision, RevisionType]}.
	 */
	private <E, R> List<RegistroHistorial<R>> consultar(Class<E> tipo, Long id, String descripcion,
			Function<E, R> mapper) {
		@SuppressWarnings("unchecked")
		List<Object[]> filas = AuditReaderFactory.get(entityManager).createQuery()
				.forRevisionsOfEntity(tipo, false, true)
				.add(AuditEntity.id().eq(id))
				.addOrder(AuditEntity.revisionNumber().asc())
				.getResultList();
		if (filas.isEmpty()) {
			throw new RecursoNoEncontradoException("No hay historial de cambios para " + descripcion + " con id " + id);
		}
		return filas.stream().map(fila -> {
			Revision revision = (Revision) fila[1];
			return new RegistroHistorial<>(
					revision.getId(),
					LocalDateTime.ofInstant(Instant.ofEpochMilli(revision.getTimestamp()), ZONA),
					revision.getUsuario(),
					operacion((RevisionType) fila[2]),
					mapper.apply(tipo.cast(fila[0])));
		}).toList();
	}

	private static TipoOperacion operacion(RevisionType tipo) {
		return switch (tipo) {
			case ADD -> TipoOperacion.CREACION;
			case MOD -> TipoOperacion.MODIFICACION;
			case DEL -> TipoOperacion.ELIMINACION;
		};
	}
}
