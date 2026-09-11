package cl.ipss.coleccion.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import cl.ipss.coleccion.entity.EstadoLamina;
import cl.ipss.coleccion.entity.TipoLamina;

/**
 * Representacion de una lamina devuelta por la API.
 *
 * @param id        identificador
 * @param albumId   album al que pertenece
 * @param numero    numero dentro del album
 * @param nombre    nombre de la lamina
 * @param tipo      categoria
 * @param cantidad  copias que tiene el coleccionista
 * @param repetidas copias sobrantes (cantidad - 1, minimo 0)
 * @param estado    FALTANTE, OBTENIDA o REPETIDA
 * @param fotoUrl   ruta para descargar la foto, o {@code null} si no tiene
 * @param auditoria quien y cuando creo o modifico la lamina (se omite en el historial)
 */
public record LaminaResponse(
		Long id,
		Long albumId,
		Integer numero,
		String nombre,
		TipoLamina tipo,
		int cantidad,
		int repetidas,
		EstadoLamina estado,
		String fotoUrl,
		@JsonInclude(JsonInclude.Include.NON_NULL) AuditoriaResponse auditoria) {
}
