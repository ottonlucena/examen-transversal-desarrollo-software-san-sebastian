package cl.ipss.coleccion.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.envers.Audited;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lamina de un album. {@code cantidad} indica cuantas copias tiene el coleccionista,
 * y de ella se derivan el estado (faltante, obtenida o repetida) y las repetidas.
 * Se mapea a la tabla {@code lamina} creada por la migracion V2.
 * {@link Audited}: Envers guarda cada version en {@code lamina_aud}.
 */
@Entity
@Table(name = "lamina")
@Getter
@Setter
@NoArgsConstructor
@Audited
public class Lamina extends EntidadAuditable {

	/** Maximo de copias que se pueden registrar de una misma lamina. */
	public static final int CANTIDAD_MAXIMA = 999;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Relacion N:1 con el album; la FK tiene ON DELETE CASCADE en la base de datos. */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "album_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Album album;

	/** Numero de la lamina dentro del album, unico por album y entre 1 y {@code totalLaminas}. */
	@Column(nullable = false)
	private Integer numero;

	@Column(nullable = false, length = 100)
	private String nombre;

	// Hibernate usaria el tipo ENUM nativo de MySQL; se fuerza VARCHAR para coincidir con la migracion
	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(nullable = false, length = 30)
	private TipoLamina tipo;

	@Column(nullable = false)
	private int cantidad;

	/** Nombre del archivo de la foto en el directorio de subidas (opcional). */
	@Column(length = 255)
	private String foto;

	/**
	 * @return estado derivado de la cantidad de copias
	 */
	public EstadoLamina getEstado() {
		return EstadoLamina.desde(cantidad);
	}

	/**
	 * @return copias sobrantes (cantidad - 1), o 0 si no esta repetida
	 */
	public int getRepetidas() {
		return Math.max(cantidad - 1, 0);
	}
}
