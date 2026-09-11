package cl.ipss.coleccion.controller;

import java.net.URI;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import cl.ipss.coleccion.dto.AlbumRequest;
import cl.ipss.coleccion.dto.AlbumResponse;
import cl.ipss.coleccion.service.AlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints REST para el CRUD de albumes. Solo recibe, valida y delega en {@link AlbumService}.
 */
@Tag(name = "1. Álbumes", description = "CRUD de álbumes de láminas")
@RestController
@RequestMapping("/api/albumes")
@RequiredArgsConstructor
public class AlbumController {

	private final AlbumService albumService;

	/**
	 * POST /api/albumes: crea un album.
	 *
	 * @param request datos del album
	 * @return 201 con el album creado y header {@code Location}
	 */
	@Operation(summary = "Crear un álbum", description = "El nombre debe ser único (sin distinguir mayúsculas).")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Álbum creado; header Location con su URL"),
			@ApiResponse(responseCode = "400", description = "Datos inválidos (detalle por campo en 'errores')"),
			@ApiResponse(responseCode = "409", description = "Ya existe un álbum con ese nombre") })
	@PostMapping
	public ResponseEntity<AlbumResponse> crear(@Valid @RequestBody AlbumRequest request) {
		AlbumResponse creado = albumService.crear(request);
		URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(creado.id()).toUri();
		return ResponseEntity.created(ubicacion).body(creado);
	}

	/**
	 * GET /api/albumes?page=0&amp;size=20&amp;sort=nombre,asc: lista paginada de albumes.
	 *
	 * @param pageable pagina, tamano (maximo 100) y orden; por defecto 20 por pagina ordenados por nombre
	 * @return pagina con {@code content} y metadatos en {@code page}
	 */
	@Operation(summary = "Listar álbumes (paginado)", description = "Parámetros page, size (máx. 100) y sort, p. ej. sort=nombre,desc.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Página de álbumes"),
			@ApiResponse(responseCode = "400", description = "Parámetro de orden inválido") })
	@GetMapping
	public PagedModel<AlbumResponse> listar(
			@ParameterObject @PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
		return new PagedModel<>(albumService.listar(pageable));
	}

	/**
	 * GET /api/albumes/{id}: obtiene un album.
	 *
	 * @param id identificador
	 * @return 200 con el album, o 404
	 */
	@Operation(summary = "Obtener un álbum")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Álbum encontrado"),
			@ApiResponse(responseCode = "404", description = "El álbum no existe") })
	@GetMapping("/{id}")
	public AlbumResponse obtener(@PathVariable Long id) {
		return albumService.obtener(id);
	}

	/**
	 * PUT /api/albumes/{id}: reemplaza los datos de un album.
	 *
	 * @param id      identificador
	 * @param request datos nuevos
	 * @return 200 con el album actualizado
	 */
	@Operation(summary = "Actualizar un álbum", description = "No permite reducir el total por debajo de la lámina de mayor número.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Álbum actualizado"),
			@ApiResponse(responseCode = "400", description = "Datos inválidos"),
			@ApiResponse(responseCode = "404", description = "El álbum no existe"),
			@ApiResponse(responseCode = "409", description = "Nombre duplicado o total menor a una lámina existente") })
	@PutMapping("/{id}")
	public AlbumResponse actualizar(@PathVariable Long id, @Valid @RequestBody AlbumRequest request) {
		return albumService.actualizar(id, request);
	}

	/**
	 * DELETE /api/albumes/{id}: elimina un album y sus laminas.
	 *
	 * @param id identificador
	 * @return 204 sin contenido
	 */
	@Operation(summary = "Eliminar un álbum", description = "Elimina también sus láminas y fotos; queda registrado en el historial.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Álbum eliminado"),
			@ApiResponse(responseCode = "404", description = "El álbum no existe") })
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		albumService.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
