package cl.ipss.coleccion.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Registro de laminas obtenidas por numero, por ejemplo al abrir sobres. Cada aparicion de un
 * numero suma una copia: {@code [1, 5, 5]} suma 1 copia a la lamina 1 y 2 copias a la 5.
 *
 * @param numeros numeros obtenidos (1 a 1000 elementos, se permiten repetidos)
 */
public record RegistroLaminasRequest(
		@Schema(description = "Números obtenidos; cada aparición suma una copia", example = "[1, 2, 2, 3, 3, 3]")
		@NotEmpty(message = "Debe enviar al menos un número")
		@Size(max = 1000, message = "No se pueden registrar más de 1000 láminas por solicitud")
		List<@NotNull(message = "El número no puede ser nulo")
			@Min(value = 1, message = "Los números deben ser mayores o iguales a 1") Integer> numeros) {
}
