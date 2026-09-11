package cl.ipss.coleccion.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.ipss.coleccion.entity.Album;

/**
 * Acceso a datos de {@link Album}.
 */
public interface AlbumRepository extends JpaRepository<Album, Long> {

	/**
	 * @param nombre nombre a buscar (sin distinguir mayusculas)
	 * @return {@code true} si ya existe un album con ese nombre
	 */
	boolean existsByNombreIgnoreCase(String nombre);

	/**
	 * Igual que {@link #existsByNombreIgnoreCase(String)} pero excluye un album; se usa al actualizar.
	 *
	 * @param nombre nombre a buscar
	 * @param id     album que se excluye de la busqueda
	 * @return {@code true} si otro album ya usa ese nombre
	 */
	boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
