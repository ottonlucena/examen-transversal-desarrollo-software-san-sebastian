package cl.ipss.coleccion.dto;

import java.time.LocalDate;

import cl.ipss.coleccion.entity.TipoLaminasAlbum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear o actualizar un album.
 *
 * @param nombre           nombre unico del album
 * @param imagen           URL http(s) de la portada (opcional)
 * @param fechaLanzamiento fecha de lanzamiento, no puede ser futura
 * @param tipoLaminas      formato de las laminas del album
 * @param totalLaminas     cantidad de laminas que componen el album
 * @param editorial        editorial (opcional)
 * @param descripcion      descripcion (opcional)
 */
public record AlbumRequest(
		@Schema(description = "Nombre único del álbum", example = "Qatar 2022")
		@NotBlank(message = "El nombre es obligatorio")
		@Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
		String nombre,

		@Schema(description = "URL http(s) de la portada (opcional)", example = "https://ejemplo.cl/portada.png")
		@Size(max = 500, message = "La URL de la imagen no puede superar los 500 caracteres")
		@Pattern(regexp = "^https?://\\S+$", message = "La imagen debe ser una URL http(s) válida")
		String imagen,

		@Schema(description = "Fecha de lanzamiento (no futura)", example = "2022-08-24")
		@NotNull(message = "La fecha de lanzamiento es obligatoria")
		@PastOrPresent(message = "La fecha de lanzamiento no puede estar en el futuro")
		LocalDate fechaLanzamiento,

		@Schema(description = "Formato de las láminas", example = "ADHESIVA")
		@NotNull(message = "El tipo de láminas es obligatorio")
		TipoLaminasAlbum tipoLaminas,

		@Schema(description = "Cantidad de láminas del álbum (1 a 2000)", example = "20")
		@NotNull(message = "El total de láminas es obligatorio")
		@Min(value = 1, message = "El álbum debe tener al menos 1 lámina")
		@Max(value = 2000, message = "El álbum no puede tener más de 2000 láminas")
		Integer totalLaminas,

		@Schema(description = "Editorial (opcional)", example = "Panini")
		@Size(max = 100, message = "La editorial no puede superar los 100 caracteres")
		String editorial,

		@Schema(description = "Descripción (opcional)", example = "Álbum oficial del mundial")
		@Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
		String descripcion) {
}
