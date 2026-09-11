package cl.ipss.coleccion.exception;

/**
 * El recurso solicitado no existe. Se responde con HTTP 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

	/**
	 * @param mensaje descripcion en espanol, visible para el cliente
	 */
	public RecursoNoEncontradoException(String mensaje) {
		super(mensaje);
	}
}
