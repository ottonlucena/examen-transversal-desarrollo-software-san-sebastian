package cl.ipss.coleccion.config;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;

import cl.ipss.coleccion.audit.UsuarioActual;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;

/**
 * Documentacion OpenAPI (Swagger UI en {@code /swagger-ui.html}).
 */
@Configuration
public class OpenApiConfig {

	/**
	 * @return metadatos de la API mostrados en Swagger UI
	 */
	@Bean
	public OpenAPI coleccionOpenApi() {
		return new OpenAPI().info(new Info()
				.title("API Colección de Láminas")
				.version("1.0.0")
				.description("""
						API REST para gestionar álbumes y láminas: CRUD, carga masiva, registro de láminas \
						obtenidas, faltantes, repetidas, fotos y auditoría de cambios. Los errores siguen el \
						formato RFC 9457 (ProblemDetail). Examen Transversal – Desarrollo de Software Web II (IPSS)."""));
	}

	/**
	 * Agrega el header opcional {@code X-Usuario} a las operaciones de escritura, para que se
	 * pueda probar la auditoria desde Swagger UI.
	 *
	 * @return personalizador de operaciones
	 */
	@Bean
	public OperationCustomizer headerUsuario() {
		return (operacion, metodo) -> {
			if (!metodo.hasMethodAnnotation(GetMapping.class)) {
				operacion.addParametersItem(new HeaderParameter()
						.name(UsuarioActual.HEADER)
						.required(false)
						.description("Usuario que realiza el cambio (auditoría). Si se omite se registra 'sistema'.")
						.schema(new StringSchema().example("ana")));
			}
			return operacion;
		};
	}
}
