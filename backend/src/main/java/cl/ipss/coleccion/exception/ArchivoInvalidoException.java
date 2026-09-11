package cl.ipss.coleccion.exception;

/**
 * El archivo enviado esta vacio o no es una imagen permitida (JPEG, PNG o WEBP).
 * Se responde con HTTP 415.
 */
public class ArchivoInvalidoException extends RuntimeException {

	/**
	 * @param mensaje descripcion en espanol, visible para el cliente
	 */
	public ArchivoInvalidoException(String mensaje) {
		super(mensaje);
	}
}
