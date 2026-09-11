package cl.ipss.coleccion.dto;

import java.util.List;

/**
 * Laminas que le faltan al coleccionista para completar el album.
 *
 * @param albumId              album consultado
 * @param totalLaminas         laminas que componen el album
 * @param totalFaltantes       suma de laminas sin copias y numeros no catalogados
 * @param laminas              laminas catalogadas con cantidad 0, ordenadas por numero
 * @param numerosNoCatalogados numeros entre 1 y {@code totalLaminas} que aun no se han cargado como lamina
 */
public record FaltantesResponse(
		Long albumId,
		int totalLaminas,
		int totalFaltantes,
		List<LaminaResponse> laminas,
		List<Integer> numerosNoCatalogados) {
}
