package cl.ipss.coleccion.dto;

import java.time.LocalDateTime;

/**
 * Una version historica de un registro, tomada de las tablas de auditoria de Envers.
 *
 * @param <T>       tipo del snapshot ({@link AlbumResponse} o {@link LaminaResponse})
 * @param revision  numero de revision (tabla {@code revinfo})
 * @param fecha     momento del cambio (zona America/Santiago)
 * @param usuario   usuario que hizo el cambio (header {@code X-Usuario})
 * @param operacion creacion, modificacion o eliminacion
 * @param datos     estado del registro en esa revision
 */
public record RegistroHistorial<T>(
		Long revision,
		LocalDateTime fecha,
		String usuario,
		TipoOperacion operacion,
		T datos) {
}
