package cl.ipss.coleccion.audit;

import java.util.regex.Pattern;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Obtiene el usuario que realiza la peticion actual desde el header {@code X-Usuario}.
 * La API no tiene autenticacion, por lo que el header identifica al autor de cada cambio en la
 * auditoria. Si falta o contiene caracteres no permitidos, se usa {@value #POR_DEFECTO}.
 */
public final class UsuarioActual {

	/** Header HTTP con el nombre del usuario. */
	public static final String HEADER = "X-Usuario";

	/** Usuario registrado cuando la peticion no informa uno valido. */
	public static final String POR_DEFECTO = "sistema";

	// Letras, numeros, punto, guion, guion bajo y arroba; maximo 100 (largo de la columna)
	private static final Pattern FORMATO = Pattern.compile("^[\\p{L}\\p{N}._@-]{1,100}$");

	private UsuarioActual() {
	}

	/**
	 * @return usuario del header {@code X-Usuario}, o {@value #POR_DEFECTO} si no hay peticion,
	 *         no viene el header o su formato no es valido
	 */
	public static String obtener() {
		if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes atributos) {
			String valor = atributos.getRequest().getHeader(HEADER);
			if (valor != null && FORMATO.matcher(valor.strip()).matches()) {
				return valor.strip();
			}
		}
		return POR_DEFECTO;
	}
}
