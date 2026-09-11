package cl.ipss.coleccion.dto;

import java.util.List;

import cl.ipss.coleccion.validation.NumerosUnicos;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Carga masiva de laminas en un album. Se procesa como una sola transaccion (todo o nada).
 *
 * @param laminas laminas a crear (1 a 500), sin numeros repetidos entre si
 */
public record LoteLaminasRequest(
		@NotEmpty(message = "Debe enviar al menos una lámina")
		@Size(max = 500, message = "El lote no puede superar las 500 láminas")
		@NumerosUnicos
		List<@NotNull(message = "La lámina no puede ser nula") @Valid LaminaRequest> laminas) {
}
