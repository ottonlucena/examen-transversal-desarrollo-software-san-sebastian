package cl.ipss.coleccion.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.ipss.coleccion.entity.Lamina;
import cl.ipss.coleccion.entity.TipoLamina;

/**
 * Acceso a datos de {@link Lamina}. Los filtros se resuelven en la base de datos.
 */
public interface LaminaRepository extends JpaRepository<Lamina, Long> {

	/**
	 * @param albumId album
	 * @return laminas del album ordenadas por numero
	 */
	List<Lamina> findByAlbumIdOrderByNumeroAsc(Long albumId);

	/**
	 * @param albumId album
	 * @param tipo    categoria a filtrar
	 * @return laminas del album de ese tipo, ordenadas por numero
	 */
	List<Lamina> findByAlbumIdAndTipoOrderByNumeroAsc(Long albumId, TipoLamina tipo);

	/**
	 * @param albumId album
	 * @param numero  numero de lamina
	 * @return {@code true} si el album ya tiene una lamina con ese numero
	 */
	boolean existsByAlbumIdAndNumero(Long albumId, Integer numero);

	/**
	 * Igual que {@link #existsByAlbumIdAndNumero(Long, Integer)} pero excluye una lamina; se usa al actualizar.
	 *
	 * @param albumId album
	 * @param numero  numero de lamina
	 * @param id      lamina que se excluye
	 * @return {@code true} si otra lamina del album usa ese numero
	 */
	boolean existsByAlbumIdAndNumeroAndIdNot(Long albumId, Integer numero, Long id);

	/**
	 * @param albumId album
	 * @return mayor numero de lamina registrado en el album, vacio si no tiene laminas
	 */
	@Query("select max(l.numero) from Lamina l where l.album.id = :albumId")
	Optional<Integer> findMaxNumeroByAlbumId(@Param("albumId") Long albumId);

	/**
	 * @param albumId album
	 * @return numeros ya catalogados en el album (solo la columna, sin cargar entidades)
	 */
	@Query("select l.numero from Lamina l where l.album.id = :albumId")
	List<Integer> findNumerosByAlbumId(@Param("albumId") Long albumId);

	/**
	 * @param albumId album
	 * @param numeros numeros buscados
	 * @return laminas del album cuyos numeros estan en la lista
	 */
	List<Lamina> findByAlbumIdAndNumeroIn(Long albumId, Collection<Integer> numeros);

	/**
	 * Faltantes: se invoca con {@code cantidad = 0}.
	 *
	 * @param albumId  album
	 * @param cantidad cantidad exacta
	 * @return laminas del album con esa cantidad, ordenadas por numero
	 */
	List<Lamina> findByAlbumIdAndCantidadOrderByNumeroAsc(Long albumId, int cantidad);

	/**
	 * Repetidas: se invoca con {@code cantidad = 1} (mas de una copia).
	 *
	 * @param albumId  album
	 * @param cantidad umbral exclusivo
	 * @return laminas del album con cantidad mayor al umbral, ordenadas por numero
	 */
	List<Lamina> findByAlbumIdAndCantidadGreaterThanOrderByNumeroAsc(Long albumId, int cantidad);

	/**
	 * Calcula en una sola consulta los totales del resumen del album.
	 *
	 * @param albumId album
	 * @return catalogadas, obtenidas, laminas repetidas y copias repetidas
	 */
	@Query("""
			select new cl.ipss.coleccion.repository.EstadisticasAlbum(
				count(l),
				coalesce(sum(case when l.cantidad >= 1 then 1L else 0L end), 0L),
				coalesce(sum(case when l.cantidad > 1 then 1L else 0L end), 0L),
				coalesce(sum(case when l.cantidad > 1 then l.cantidad - 1L else 0L end), 0L))
			from Lamina l
			where l.album.id = :albumId
			""")
	EstadisticasAlbum calcularEstadisticas(@Param("albumId") Long albumId);
}
