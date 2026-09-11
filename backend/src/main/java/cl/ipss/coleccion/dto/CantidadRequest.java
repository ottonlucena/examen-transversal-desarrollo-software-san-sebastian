package cl.ipss.coleccion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Ajuste de la cantidad de copias de una lamina.
 *
 * @param delta copias a sumar (positivo) o restar (negativo); la cantidad final debe quedar entre 0 y 999
 */
public record CantidadRequest(
		@Schema(description = "Copias a sumar (positivo) o restar (negativo)", example = "-1")
		@NotNull(message = "El delta es obligatorio")
		@Min(value = -999, message = "El delta no puede ser menor a -999")
		@Max(value = 999, message = "El delta no puede ser mayor a 999")
		Integer delta) {
}
