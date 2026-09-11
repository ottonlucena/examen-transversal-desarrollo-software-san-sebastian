package cl.ipss.coleccion.entity;

import java.time.LocalDate;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Album de laminas. Define cuantas laminas lo componen ({@code totalLaminas}) y su formato.
 * Se mapea a la tabla {@code album} creada por la migracion V1. Sus laminas lo referencian
 * mediante {@link Lamina#getAlbum()}; al eliminar un album, la base de datos elimina sus
 * laminas en cascada. {@link Audited}: Envers guarda cada version en {@code album_aud}.
 */
@Entity
@Table(name = "album")
@Getter
@Setter
@NoArgsConstructor
@Audited
public class Album extends EntidadAuditable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String nombre;

	/** URL de la imagen de portada (opcional). */
	@Column(length = 500)
	private String imagen;

	@Column(name = "fecha_lanzamiento", nullable = false)
	private LocalDate fechaLanzamiento;

	// Hibernate usaria el tipo ENUM nativo de MySQL; se fuerza VARCHAR para coincidir con la migracion
	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "tipo_laminas", nullable = false, length = 30)
	private TipoLaminasAlbum tipoLaminas;

	@Column(name = "total_laminas", nullable = false)
	private Integer totalLaminas;

	@Column(length = 100)
	private String editorial;

	@Column(length = 500)
	private String descripcion;
}
