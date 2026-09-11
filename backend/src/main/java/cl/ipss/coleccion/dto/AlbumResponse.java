package cl.ipss.coleccion.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;

import cl.ipss.coleccion.entity.TipoLaminasAlbum;

/**
 * Representacion de un album devuelta por la API.
 *
 * @param id               identificador
 * @param nombre           nombre del album
 * @param imagen           URL de la portada, o {@code null}
 * @param fechaLanzamiento fecha de lanzamiento
 * @param tipoLaminas      formato de las laminas
 * @param totalLaminas     cantidad de laminas del album
 * @param editorial        editorial, o {@code null}
 * @param descripcion      descripcion, o {@code null}
 * @param auditoria        quien y cuando creo o modifico el album (se omite en el historial)
 */
public record AlbumResponse(
		Long id,
		String nombre,
		String imagen,
		LocalDate fechaLanzamiento,
		TipoLaminasAlbum tipoLaminas,
		Integer totalLaminas,
		String editorial,
		String descripcion,
		@JsonInclude(JsonInclude.Include.NON_NULL) AuditoriaResponse auditoria) {
}
