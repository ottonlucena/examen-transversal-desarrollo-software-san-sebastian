package cl.ipss.coleccion.repository;

/**
 * Proyeccion con los totales de un album, calculados por la base de datos en una sola consulta
 * (ver {@link LaminaRepository#calcularEstadisticas(Long)}).
 *
 * @param catalogadas      laminas registradas en el album
 * @param obtenidas        laminas con al menos una copia
 * @param laminasRepetidas laminas con mas de una copia
 * @param copiasRepetidas  suma de copias sobrantes (cantidad - 1)
 */
public record EstadisticasAlbum(Long catalogadas, Long obtenidas, Long laminasRepetidas, Long copiasRepetidas) {
}
