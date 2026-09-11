package cl.ipss.coleccion.mapper;

import cl.ipss.coleccion.dto.LaminaRequest;
import cl.ipss.coleccion.dto.LaminaResponse;
import cl.ipss.coleccion.entity.Album;
import cl.ipss.coleccion.entity.Lamina;

/**
 * Conversion entre {@link Lamina} y sus DTOs. Evita exponer la entidad JPA en la API.
 */
public final class LaminaMapper {

	private LaminaMapper() {
	}

	/**
	 * Crea una lamina nueva del album indicado. Si el request no trae cantidad, parte en 0 (faltante).
	 *
	 * @param request datos validados
	 * @param album   album al que pertenece
	 * @return entidad lista para persistir
	 */
	public static Lamina toEntity(LaminaRequest request, Album album) {
		Lamina lamina = new Lamina();
		lamina.setAlbum(album);
		lamina.setCantidad(0);
		actualizar(lamina, request);
		return lamina;
	}

	/**
	 * Copia los datos del request sobre una lamina existente. La cantidad solo se modifica
	 * si viene informada.
	 *
	 * @param lamina  entidad a modificar
	 * @param request datos validados
	 */
	public static void actualizar(Lamina lamina, LaminaRequest request) {
		lamina.setNumero(request.numero());
		lamina.setNombre(request.nombre().strip());
		lamina.setTipo(request.tipo());
		if (request.cantidad() != null) {
			lamina.setCantidad(request.cantidad());
		}
	}

	/**
	 * @param lamina entidad
	 * @return DTO de respuesta con estado y repetidas calculados
	 */
	public static LaminaResponse toResponse(Lamina lamina) {
		String fotoUrl = lamina.getFoto() == null ? null : "/api/laminas/" + lamina.getId() + "/foto";
		return new LaminaResponse(
				lamina.getId(),
				lamina.getAlbum().getId(),
				lamina.getNumero(),
				lamina.getNombre(),
				lamina.getTipo(),
				lamina.getCantidad(),
				lamina.getRepetidas(),
				lamina.getEstado(),
				fotoUrl,
				AuditoriaMapper.toResponse(lamina));
	}
}
