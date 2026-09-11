package cl.ipss.coleccion.entity;

import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import cl.ipss.coleccion.audit.RevisionUsuarioListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Revision de Hibernate Envers: una fila por cada transaccion que modifica entidades auditadas.
 * Ademas del numero y la fecha, guarda el usuario que hizo el cambio. Las tablas
 * {@code album_aud} y {@code lamina_aud} referencian esta tabla ({@code revinfo}, migracion V4).
 */
@Entity
@Table(name = "revinfo")
@RevisionEntity(RevisionUsuarioListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Revision {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@RevisionNumber
	private Long id;

	/** Momento de la revision en milisegundos desde epoch (tipo requerido por Envers). */
	@RevisionTimestamp
	@Column(name = "revtstmp", nullable = false)
	private long timestamp;

	@Column(length = 100)
	private String usuario;
}
