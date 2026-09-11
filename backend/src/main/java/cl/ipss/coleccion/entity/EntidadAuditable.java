package cl.ipss.coleccion.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * Clase base con los campos de auditoria que Spring Data JPA completa automaticamente al
 * insertar y al actualizar: fecha y usuario de creacion y de ultima modificacion. El usuario
 * se obtiene del header {@code X-Usuario} (ver {@code JpaAuditingConfig}). Las columnas se
 * agregaron con la migracion V3.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class EntidadAuditable {

	@CreatedDate
	@Column(name = "creado_en", nullable = false, updatable = false)
	private LocalDateTime creadoEn;

	@CreatedBy
	@Column(name = "creado_por", nullable = false, updatable = false, length = 100)
	private String creadoPor;

	@LastModifiedDate
	@Column(name = "modificado_en", nullable = false)
	private LocalDateTime modificadoEn;

	@LastModifiedBy
	@Column(name = "modificado_por", nullable = false, length = 100)
	private String modificadoPor;
}
