package cl.ipss.coleccion.exception;

import java.util.Map;

/**
 * La operacion es valida en formato pero entra en conflicto con una regla de negocio
 * o con el estado actual de los datos (duplicados, rangos). Se responde con HTTP 409.
 * Puede incluir un detalle por campo, por ejemplo para indicar que posiciones de un lote fallaron.
 */
public class ReglaNegocioException extends RuntimeException {

	private final Map<String, String> errores;

	/**
	 * @param mensaje descripcion en espanol, visible para el cliente
	 */
	public ReglaNegocioException(String mensaje) {
		this(mensaje, Map.of());
	}

	/**
	 * @param mensaje descripcion general en espanol
	 * @param errores detalle por campo o posicion (se devuelve en la propiedad {@code errores})
	 */
	public ReglaNegocioException(String mensaje, Map<String, String> errores) {
		super(mensaje);
		this.errores = Map.copyOf(errores);
	}

	/**
	 * @return detalle por campo; vacio si no aplica
	 */
	public Map<String, String> getErrores() {
		return errores;
	}
}
