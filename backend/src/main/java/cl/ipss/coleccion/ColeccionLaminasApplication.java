package cl.ipss.coleccion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la API REST de coleccion de laminas.
 * <p>
 * Arranca Spring Boot con la configuracion de {@code application.properties}: conexion a MySQL,
 * migraciones Flyway, JPA Auditing, Hibernate Envers y documentacion OpenAPI.
 */
@SpringBootApplication
public class ColeccionLaminasApplication {

	/**
	 * Inicia la aplicacion.
	 *
	 * @param args argumentos de linea de comandos, que se pasan a Spring Boot
	 */
	public static void main(String[] args) {
		SpringApplication.run(ColeccionLaminasApplication.class, args);
	}

}
