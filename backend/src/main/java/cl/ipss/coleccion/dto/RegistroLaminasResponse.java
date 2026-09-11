package cl.ipss.coleccion.dto;

import java.util.List;

/**
 * Resultado de registrar laminas obtenidas.
 *
 * @param copiasRegistradas total de copias sumadas (largo de la lista recibida)
 * @param laminas           laminas afectadas con su nueva cantidad, ordenadas por numero
 */
public record RegistroLaminasResponse(int copiasRegistradas, List<LaminaResponse> laminas) {
}
