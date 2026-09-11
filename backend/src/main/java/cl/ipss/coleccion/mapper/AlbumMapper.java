package cl.ipss.coleccion.mapper;

import cl.ipss.coleccion.dto.AlbumRequest;
import cl.ipss.coleccion.dto.AlbumResponse;
import cl.ipss.coleccion.entity.Album;

/**
 * Conversion entre {@link Album} y sus DTOs. Evita exponer la entidad JPA en la API.
 */
public final class AlbumMapper {

	private AlbumMapper() {
	}

	/**
	 * Crea una entidad nueva (sin id) a partir del request.
	 *
	 * @param request datos validados
	 * @return entidad lista para persistir
	 */
	public static Album toEntity(AlbumRequest request) {
		Album album = new Album();
		actualizar(album, request);
		return album;
	}

	/**
	 * Copia los datos del request sobre una entidad existente. Los textos se recortan y
	 * los opcionales vacios se guardan como {@code null}.
	 *
	 * @param album   entidad a modificar
	 * @param request datos validados
	 */
	public static void actualizar(Album album, AlbumRequest request) {
		album.setNombre(request.nombre().strip());
		album.setImagen(textoOpcional(request.imagen()));
		album.setFechaLanzamiento(request.fechaLanzamiento());
		album.setTipoLaminas(request.tipoLaminas());
		album.setTotalLaminas(request.totalLaminas());
		album.setEditorial(textoOpcional(request.editorial()));
		album.setDescripcion(textoOpcional(request.descripcion()));
	}

	/**
	 * @param album entidad
	 * @return DTO de respuesta
	 */
	public static AlbumResponse toResponse(Album album) {
		return new AlbumResponse(
				album.getId(),
				album.getNombre(),
				album.getImagen(),
				album.getFechaLanzamiento(),
				album.getTipoLaminas(),
				album.getTotalLaminas(),
				album.getEditorial(),
				album.getDescripcion(),
				AuditoriaMapper.toResponse(album));
	}

	private static String textoOpcional(String valor) {
		return (valor == null || valor.isBlank()) ? null : valor.strip();
	}
}
