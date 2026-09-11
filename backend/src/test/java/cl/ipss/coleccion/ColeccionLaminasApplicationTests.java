package cl.ipss.coleccion;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Verifica que el contexto de Spring arranque contra MySQL real (Testcontainers),
 * lo que incluye aplicar todas las migraciones Flyway y validar el esquema.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ColeccionLaminasApplicationTests {

	@Test
	void contextLoads() {
	}

}
