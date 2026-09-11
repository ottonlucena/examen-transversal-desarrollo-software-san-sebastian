package cl.ipss.coleccion.service;

import java.util.List;

import cl.ipss.coleccion.dto.CantidadRequest;
import cl.ipss.coleccion.dto.FaltantesResponse;
import cl.ipss.coleccion.dto.LaminaResponse;
import cl.ipss.coleccion.dto.LoteLaminasRequest;
import cl.ipss.coleccion.dto.RegistroLaminasRequest;
import cl.ipss.coleccion.dto.RegistroLaminasResponse;
import cl.ipss.coleccion.dto.RepetidasResponse;
import cl.ipss.coleccion.dto.ResumenAlbumResponse;
import cl.ipss.coleccion.exception.RecursoNoEncontradoException;
import cl.ipss.coleccion.exception.ReglaNegocioException;

/**
 * Funcionalidades especiales de la coleccion: carga masiva, registro de laminas obtenidas,
 * gestion del estado (cantidad) y listados de faltantes y repetidas.
 */
public interface ColeccionService {

	/**
	 * Crea varias laminas en un album en una sola transaccion. Si alguna falla, no se crea ninguna.
	 *
	 * @param albumId album destino
	 * @param request laminas a crear (sin numeros repetidos entre si)
	 * @return laminas creadas ordenadas por numero
	 * @throws RecursoNoEncontradoException si el album no existe
	 * @throws ReglaNegocioException        si algun numero excede el total o ya existe en el album
	 *                                      (el detalle indica cada posicion con problema)
	 */
	List<LaminaResponse> cargarLote(Long albumId, LoteLaminasRequest request);

	/**
	 * Suma una copia por cada numero recibido. Todo o nada.
	 *
	 * @param albumId album
	 * @param request numeros obtenidos (pueden repetirse)
	 * @return copias registradas y laminas afectadas
	 * @throws RecursoNoEncontradoException si el album no existe
	 * @throws ReglaNegocioException        si algun numero no esta catalogado o se supera el maximo de copias
	 */
	RegistroLaminasResponse registrarObtenidas(Long albumId, RegistroLaminasRequest request);

	/**
	 * Suma o resta copias a una lamina.
	 *
	 * @param laminaId lamina
	 * @param request  delta a aplicar
	 * @return lamina con su nueva cantidad y estado
	 * @throws RecursoNoEncontradoException si la lamina no existe
	 * @throws ReglaNegocioException        si la cantidad quedaria fuera de 0..999
	 */
	LaminaResponse ajustarCantidad(Long laminaId, CantidadRequest request);

	/**
	 * @param albumId album
	 * @return laminas sin copias y numeros aun no catalogados
	 * @throws RecursoNoEncontradoException si el album no existe
	 */
	FaltantesResponse faltantes(Long albumId);

	/**
	 * @param albumId album
	 * @return laminas con mas de una copia y cuantas repetidas hay de cada una
	 * @throws RecursoNoEncontradoException si el album no existe
	 */
	RepetidasResponse repetidas(Long albumId);

	/**
	 * @param albumId album
	 * @return totales y porcentaje de avance del album
	 * @throws RecursoNoEncontradoException si el album no existe
	 */
	ResumenAlbumResponse resumen(Long albumId);
}
