package cl.ipss.coleccion.dto;

import cl.ipss.coleccion.entity.Lamina;
import cl.ipss.coleccion.entity.TipoLamina;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear o actualizar una lamina.
 *
 * @param numero   numero de la lamina dentro del album (1..totalLaminas)
 * @param nombre   nombre o descripcion de la lamina
 * @param tipo     categoria de la lamina
 * @param cantidad copias que tiene el coleccionista; opcional: al crear se asume 0
 *                 y al actualizar, si viene {@code null}, se conserva la actual
 */
public record LaminaRequest(
		@Schema(description = "Número dentro del álbum (1..totalLaminas)", example = "7")
		@NotNull(message = "El número de la lámina es obligatorio")
		@Min(value = 1, message = "El número de la lámina debe ser mayor o igual a 1")
		Integer numero,

		@Schema(description = "Nombre de la lámina", example = "Escudo Chile")
		@NotBlank(message = "El nombre de la lámina es obligatorio")
		@Size(max = 100, message = "El nombre de la lámina no puede superar los 100 caracteres")
		String nombre,

		@Schema(description = "Categoría de la lámina", example = "ESCUDO")
		@NotNull(message = "El tipo de lámina es obligatorio")
		TipoLamina tipo,

		@Schema(description = "Copias que tiene el coleccionista (opcional, 0 a 999)", example = "0")
		@Min(value = 0, message = "La cantidad no puede ser negativa")
		@Max(value = Lamina.CANTIDAD_MAXIMA, message = "La cantidad no puede superar 999")
		Integer cantidad) {
}
