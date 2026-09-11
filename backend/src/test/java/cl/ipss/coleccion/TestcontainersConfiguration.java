package cl.ipss.coleccion;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Levanta un contenedor MySQL desechable para las pruebas de integracion.
 * Usa la misma version que compose.yaml, asi las migraciones Flyway se prueban
 * contra el mismo motor que en ejecucion. {@link ServiceConnection} configura
 * el datasource automaticamente.
 */
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	MySQLContainer mysqlContainer() {
		return new MySQLContainer(DockerImageName.parse("mysql:8.4"));
	}

}
