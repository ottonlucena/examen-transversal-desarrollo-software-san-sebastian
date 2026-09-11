package cl.ipss.coleccion.mapper;

import cl.ipss.coleccion.dto.AuditoriaResponse;
import cl.ipss.coleccion.entity.EntidadAuditable;

/**
 * Conversion de los campos de {@link EntidadAuditable} a {@link AuditoriaResponse}.
 */
public final class AuditoriaMapper {

	private AuditoriaMapper() {
	}

	/**
	 * @param entidad entidad auditable
	 * @return datos de auditoria, o {@code null} si la entidad no los tiene (por ejemplo, las
	 *         versiones historicas de Envers, que no guardan estos campos) y asi no se serializan
	 */
	public static AuditoriaResponse toResponse(EntidadAuditable entidad) {
		if (entidad.getCreadoEn() == null) {
			return null;
		}
		return new AuditoriaResponse(entidad.getCreadoEn(), entidad.getCreadoPor(),
				entidad.getModificadoEn(), entidad.getModificadoPor());
	}
}
