package cl.ipss.coleccion.config;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import cl.ipss.coleccion.audit.UsuarioActual;

/**
 * Activa la auditoria de Spring Data JPA: completa {@code creadoEn/creadoPor} al insertar y
 * {@code modificadoEn/modificadoPor} en cada actualizacion de las entidades que extienden
 * {@code EntidadAuditable}.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorActual", dateTimeProviderRef = "fechaActual")
public class JpaAuditingConfig {

	/**
	 * @return proveedor del usuario actual, tomado del header {@code X-Usuario}
	 */
	@Bean
	public AuditorAware<String> auditorActual() {
		return () -> Optional.of(UsuarioActual.obtener());
	}

	/**
	 * Fecha actual truncada a microsegundos, la precision de las columnas {@code DATETIME(6)}.
	 * Asi el valor que devuelve la API al crear es identico al que queda guardado (MySQL
	 * redondearia los nanosegundos).
	 *
	 * @return proveedor de la fecha de auditoria
	 */
	@Bean
	public DateTimeProvider fechaActual() {
		return () -> Optional.of(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
	}
}
