package cl.ipss.coleccion.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import cl.ipss.coleccion.dto.AlbumRequest;
import cl.ipss.coleccion.dto.AlbumResponse;
import cl.ipss.coleccion.exception.RecursoNoEncontradoException;
import cl.ipss.coleccion.exception.ReglaNegocioException;

/**
 * Logica de negocio de los albumes.
 */
public interface AlbumService {

	/**
	 * Crea un album.
	 *
	 * @param request datos validados
	 * @return album creado
	 * @throws ReglaNegocioException si ya existe un album con el mismo nombre
	 */
	AlbumResponse crear(AlbumRequest request);

	/**
	 * Lista los albumes de forma paginada.
	 *
	 * @param pageable pagina, tamano y orden
	 * @return pagina de albumes
	 */
	Page<AlbumResponse> listar(Pageable pageable);

	/**
	 * @param id identificador
	 * @return album encontrado
	 * @throws RecursoNoEncontradoException si no existe
	 */
	AlbumResponse obtener(Long id);

	/**
	 * Actualiza todos los datos de un album.
	 *
	 * @param id      identificador
	 * @param request datos validados
	 * @return album actualizado
	 * @throws RecursoNoEncontradoException si no existe
	 * @throws ReglaNegocioException        si el nombre ya lo usa otro album o si el nuevo
	 *                                      total deja fuera de rango laminas existentes
	 */
	AlbumResponse actualizar(Long id, AlbumRequest request);

	/**
	 * Elimina un album y, en cascada, todas sus laminas.
	 *
	 * @param id identificador
	 * @throws RecursoNoEncontradoException si no existe
	 */
	void eliminar(Long id);
}
