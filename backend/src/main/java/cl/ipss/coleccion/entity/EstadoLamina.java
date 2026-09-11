package cl.ipss.coleccion.entity;

/**
 * Estado de una lamina segun cuantas copias tiene el coleccionista.
 * No se persiste: se deriva siempre de {@link Lamina#getCantidad()}.
 */
public enum EstadoLamina {
	/** El coleccionista no tiene ninguna copia (cantidad = 0). */
	FALTANTE,
	/** Tiene exactamente una copia (cantidad = 1). */
	OBTENIDA,
	/** Tiene mas de una copia (cantidad &gt; 1). */
	REPETIDA;

	/**
	 * Calcula el estado a partir de la cantidad de copias.
	 *
	 * @param cantidad copias que tiene el coleccionista (&gt;= 0)
	 * @return estado correspondiente
	 */
	public static EstadoLamina desde(int cantidad) {
		if (cantidad == 0) {
			return FALTANTE;
		}
		return cantidad == 1 ? OBTENIDA : REPETIDA;
	}
}
