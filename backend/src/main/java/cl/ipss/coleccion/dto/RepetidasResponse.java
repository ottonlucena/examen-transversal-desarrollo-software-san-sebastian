package cl.ipss.coleccion.dto;

import java.util.List;

/**
 * Laminas repetidas del album. Cada elemento de {@code laminas} indica en {@code repetidas}
 * cuantas copias sobrantes hay de esa lamina.
 *
 * @param albumId              album consultado
 * @param laminasRepetidas     cantidad de laminas distintas con mas de una copia
 * @param totalCopiasRepetidas suma de copias sobrantes (disponibles para intercambio)
 * @param laminas              laminas con cantidad &gt; 1, ordenadas por numero
 */
public record RepetidasResponse(
		Long albumId,
		int laminasRepetidas,
		int totalCopiasRepetidas,
		List<LaminaResponse> laminas) {
}
