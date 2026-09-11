package cl.ipss.coleccion.service;

import java.util.List;

import cl.ipss.coleccion.dto.AlbumResponse;
import cl.ipss.coleccion.dto.LaminaResponse;
import cl.ipss.coleccion.dto.RegistroHistorial;
import cl.ipss.coleccion.exception.RecursoNoEncontradoException;

/**
 * Consulta del historial de cambios (auditoria Hibernate Envers). Incluye registros ya
 * eliminados, cuya ultima version muestra los datos que tenian al borrarse.
 */
public interface HistorialService {

	/**
	 * @param albumId album (puede estar eliminado)
	 * @return versiones del album ordenadas de la mas antigua a la mas reciente
	 * @throws RecursoNoEncontradoException si nunca existio un album con ese id
	 */
	List<RegistroHistorial<AlbumResponse>> historialAlbum(Long albumId);

	/**
	 * @param laminaId lamina (puede estar eliminada)
	 * @return versiones de la lamina ordenadas de la mas antigua a la mas reciente
	 * @throws RecursoNoEncontradoException si nunca existio una lamina con ese id
	 */
	List<RegistroHistorial<LaminaResponse>> historialLamina(Long laminaId);
}
