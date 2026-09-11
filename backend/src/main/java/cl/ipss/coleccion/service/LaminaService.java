package cl.ipss.coleccion.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import cl.ipss.coleccion.dto.ArchivoFoto;
import cl.ipss.coleccion.dto.LaminaRequest;
import cl.ipss.coleccion.dto.LaminaResponse;
import cl.ipss.coleccion.entity.TipoLamina;
import cl.ipss.coleccion.exception.ArchivoInvalidoException;
import cl.ipss.coleccion.exception.RecursoNoEncontradoException;
import cl.ipss.coleccion.exception.ReglaNegocioException;

/**
 * Logica de negocio de las laminas.
 */
public interface LaminaService {

	/**
	 * Agrega una lamina a un album.
	 *
	 * @param albumId album destino
	 * @param request datos validados
	 * @return lamina creada
	 * @throws RecursoNoEncontradoException si el album no existe
	 * @throws ReglaNegocioException        si el numero excede el total del album o ya existe en el
	 */
	LaminaResponse crear(Long albumId, LaminaRequest request);

	/**
	 * Lista las laminas de un album ordenadas por numero.
	 *
	 * @param albumId album
	 * @param tipo    filtro opcional por categoria ({@code null} = todas)
	 * @return laminas del album
	 * @throws RecursoNoEncontradoException si el album no existe
	 */
	List<LaminaResponse> listarPorAlbum(Long albumId, TipoLamina tipo);

	/**
	 * @param id identificador
	 * @return lamina encontrada
	 * @throws RecursoNoEncontradoException si no existe
	 */
	LaminaResponse obtener(Long id);

	/**
	 * Actualiza una lamina. El album al que pertenece no cambia.
	 *
	 * @param id      identificador
	 * @param request datos validados
	 * @return lamina actualizada
	 * @throws RecursoNoEncontradoException si no existe
	 * @throws ReglaNegocioException        si el nuevo numero excede el total del album o ya existe en el
	 */
	LaminaResponse actualizar(Long id, LaminaRequest request);

	/**
	 * Elimina una lamina y su foto, si tiene.
	 *
	 * @param id identificador
	 * @throws RecursoNoEncontradoException si no existe
	 */
	void eliminar(Long id);

	/**
	 * Sube o reemplaza la foto de una lamina. La foto anterior se borra al confirmar la transaccion.
	 *
	 * @param id      lamina
	 * @param archivo imagen JPEG, PNG o WEBP
	 * @return lamina con su {@code fotoUrl}
	 * @throws RecursoNoEncontradoException si la lamina no existe
	 * @throws ArchivoInvalidoException     si el archivo esta vacio o no es una imagen permitida
	 */
	LaminaResponse subirFoto(Long id, MultipartFile archivo);

	/**
	 * @param id lamina
	 * @return contenido y tipo MIME de la foto
	 * @throws RecursoNoEncontradoException si la lamina no existe o no tiene foto
	 */
	ArchivoFoto obtenerFoto(Long id);

	/**
	 * Quita la foto de la lamina y borra el archivo.
	 *
	 * @param id lamina
	 * @throws RecursoNoEncontradoException si la lamina no existe o no tiene foto
	 */
	void eliminarFoto(Long id);
}
