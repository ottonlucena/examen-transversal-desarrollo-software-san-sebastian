package cl.ipss.coleccion.dto;

import java.time.LocalDateTime;

/**
 * Datos de auditoria de un registro: quien y cuando lo creo y lo modifico por ultima vez.
 *
 * @param creadoEn      fecha de creacion
 * @param creadoPor     usuario que lo creo (header {@code X-Usuario})
 * @param modificadoEn  fecha de la ultima modificacion
 * @param modificadoPor usuario de la ultima modificacion
 */
public record AuditoriaResponse(
		LocalDateTime creadoEn,
		String creadoPor,
		LocalDateTime modificadoEn,
		String modificadoPor) {
}
