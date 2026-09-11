package cl.ipss.coleccion.dto;

/**
 * Estado general de la coleccion de un album.
 *
 * @param albumId              album consultado
 * @param nombre               nombre del album
 * @param totalLaminas         laminas que componen el album
 * @param catalogadas          laminas cargadas en el catalogo del album
 * @param obtenidas            laminas distintas con al menos una copia
 * @param faltantes            laminas que aun no se tienen ({@code totalLaminas - obtenidas})
 * @param laminasRepetidas     laminas distintas con mas de una copia
 * @param copiasRepetidas      suma de copias sobrantes
 * @param porcentajeCompletado porcentaje de avance del album (0 a 100, un decimal)
 */
public record ResumenAlbumResponse(
		Long albumId,
		String nombre,
		int totalLaminas,
		int catalogadas,
		int obtenidas,
		int faltantes,
		int laminasRepetidas,
		int copiasRepetidas,
		double porcentajeCompletado) {
}
