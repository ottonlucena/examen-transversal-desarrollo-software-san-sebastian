package cl.ipss.coleccion.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * Traduce todas las excepciones a respuestas {@link ProblemDetail} (RFC 9457) con mensajes en espanol.
 * Hereda de {@link ResponseEntityExceptionHandler} para cubrir tambien los errores estandar de
 * Spring MVC (JSON mal formado, metodo no permitido, ruta inexistente, etc.).
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	/**
	 * @param ex recurso inexistente
	 * @return 404
	 */
	@ExceptionHandler(RecursoNoEncontradoException.class)
	public ProblemDetail manejarNoEncontrado(RecursoNoEncontradoException ex) {
		return problema(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
	}

	/**
	 * @param ex regla de negocio incumplida
	 * @return 409
	 */
	@ExceptionHandler(ReglaNegocioException.class)
	public ProblemDetail manejarReglaNegocio(ReglaNegocioException ex) {
		ProblemDetail detalle = problema(HttpStatus.CONFLICT, "Conflicto con las reglas de negocio", ex.getMessage());
		if (!ex.getErrores().isEmpty()) {
			detalle.setProperty("errores", ex.getErrores());
		}
		return detalle;
	}

	/**
	 * @param ex archivo vacio o que no es una imagen permitida
	 * @return 415
	 */
	@ExceptionHandler(ArchivoInvalidoException.class)
	public ProblemDetail manejarArchivoInvalido(ArchivoInvalidoException ex) {
		return problema(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Archivo no soportado", ex.getMessage());
	}

	/**
	 * Respaldo ante restricciones de la base de datos (UNIQUE, FK, CHECK) que no alcanzo a
	 * detectar el service, por ejemplo por dos peticiones simultaneas.
	 *
	 * @param ex violacion de integridad
	 * @return 409
	 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ProblemDetail manejarIntegridad(DataIntegrityViolationException ex) {
		log.warn("Violacion de integridad: {}", ex.getMostSpecificCause().getMessage());
		return problema(HttpStatus.CONFLICT, "Violación de integridad de datos",
				"La operación viola una restricción de la base de datos (dato duplicado o referencia inválida)");
	}

	/**
	 * @param ex ordenamiento por una propiedad inexistente ({@code ?sort=...})
	 * @return 400
	 */
	@ExceptionHandler(PropertyReferenceException.class)
	public ProblemDetail manejarOrdenInvalido(PropertyReferenceException ex) {
		return problema(HttpStatus.BAD_REQUEST, "Parámetro inválido",
				"No se puede ordenar por la propiedad '" + ex.getPropertyName() + "'");
	}

	/**
	 * @param ex cualquier error no previsto
	 * @return 500 sin exponer detalles internos
	 */
	@ExceptionHandler(Exception.class)
	public ProblemDetail manejarErrorInesperado(Exception ex) {
		log.error("Error inesperado", ex);
		return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "Ocurrió un error inesperado");
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Map<String, String> errores = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(error -> errores.putIfAbsent(error.getField(), error.getDefaultMessage()));
		ProblemDetail detalle = problema(HttpStatus.BAD_REQUEST, "Datos inválidos",
				"La solicitud contiene errores de validación");
		detalle.setProperty("errores", errores);
		return handleExceptionInternal(ex, detalle, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		ProblemDetail detalle = problema(HttpStatus.BAD_REQUEST, "Solicitud mal formada",
				"El cuerpo no es un JSON válido o contiene valores no permitidos (revise fechas y tipos)");
		return handleExceptionInternal(ex, detalle, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
			HttpStatusCode status, WebRequest request) {
		String parametro = ex instanceof MethodArgumentTypeMismatchException m ? m.getName() : ex.getPropertyName();
		ProblemDetail detalle = problema(HttpStatus.BAD_REQUEST, "Parámetro inválido",
				"El parámetro '" + parametro + "' tiene un valor inválido: '" + ex.getValue() + "'");
		return handleExceptionInternal(ex, detalle, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		ProblemDetail detalle = problema(HttpStatus.PAYLOAD_TOO_LARGE, "Archivo demasiado grande",
				"La foto supera el tamaño máximo permitido (5 MB)");
		return handleExceptionInternal(ex, detalle, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		ProblemDetail detalle = problema(HttpStatus.BAD_REQUEST, "Falta el archivo",
				"Envíe la foto en el campo '" + ex.getRequestPartName() + "' como multipart/form-data");
		return handleExceptionInternal(ex, detalle, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		ProblemDetail detalle = problema(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de contenido no soportado",
				"El Content-Type '" + ex.getContentType() + "' no es aceptado por este endpoint. Soportados: "
						+ ex.getSupportedMediaTypes());
		return handleExceptionInternal(ex, detalle, headers, status, request);
	}

	private static ProblemDetail problema(HttpStatus status, String titulo, String detalle) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalle);
		problema.setTitle(titulo);
		problema.setProperty("timestamp", Instant.now());
		return problema;
	}
}
