package cl.ipss.coleccion.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import cl.ipss.coleccion.dto.LaminaRequest;
import cl.ipss.coleccion.dto.LaminaResponse;
import cl.ipss.coleccion.entity.TipoLamina;
import cl.ipss.coleccion.service.LaminaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints REST para el CRUD de laminas. Las laminas se crean y listan dentro de su album
 * ({@code /api/albumes/{albumId}/laminas}) y se consultan, modifican o eliminan por su id
 * ({@code /api/laminas/{id}}).
 */
@Tag(name = "2. Láminas", description = "CRUD de láminas de un álbum")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LaminaController {

	private final LaminaService laminaService;

	/**
	 * POST /api/albumes/{albumId}/laminas: agrega una lamina al album.
	 *
	 * @param albumId album destino
	 * @param request datos de la lamina
	 * @return 201 con la lamina creada y header {@code Location}
	 */
	@Operation(summary = "Agregar una lámina a un álbum", description = "El número debe estar entre 1 y el total del álbum y no repetirse en él.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Lámina creada; header Location con su URL"),
			@ApiResponse(responseCode = "400", description = "Datos inválidos"),
			@ApiResponse(responseCode = "404", description = "El álbum no existe"),
			@ApiResponse(responseCode = "409", description = "Número fuera de rango o ya usado en el álbum") })
	@PostMapping("/albumes/{albumId}/laminas")
	public ResponseEntity<LaminaResponse> crear(@PathVariable Long albumId,
			@Valid @RequestBody LaminaRequest request) {
		LaminaResponse creada = laminaService.crear(albumId, request);
		URI ubicacion = ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/api/laminas/{id}").buildAndExpand(creada.id()).toUri();
		return ResponseEntity.created(ubicacion).body(creada);
	}

	/**
	 * GET /api/albumes/{albumId}/laminas?tipo=BRILLANTE: laminas del album ordenadas por numero.
	 *
	 * @param albumId album
	 * @param tipo    filtro opcional por categoria
	 * @return 200 con la lista (vacia si el album no tiene laminas)
	 */
	@Operation(summary = "Listar las láminas de un álbum", description = "Ordenadas por número; filtro opcional por tipo.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Láminas del álbum"),
			@ApiResponse(responseCode = "400", description = "Tipo inválido"),
			@ApiResponse(responseCode = "404", description = "El álbum no existe") })
	@GetMapping("/albumes/{albumId}/laminas")
	public List<LaminaResponse> listar(@PathVariable Long albumId,
			@RequestParam(required = false) TipoLamina tipo) {
		return laminaService.listarPorAlbum(albumId, tipo);
	}

	/**
	 * GET /api/laminas/{id}: obtiene una lamina.
	 *
	 * @param id identificador
	 * @return 200 con la lamina, o 404
	 */
	@Operation(summary = "Obtener una lámina")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Lámina encontrada"),
			@ApiResponse(responseCode = "404", description = "La lámina no existe") })
	@GetMapping("/laminas/{id}")
	public LaminaResponse obtener(@PathVariable Long id) {
		return laminaService.obtener(id);
	}

	/**
	 * PUT /api/laminas/{id}: actualiza una lamina.
	 *
	 * @param id      identificador
	 * @param request datos nuevos
	 * @return 200 con la lamina actualizada
	 */
	@Operation(summary = "Actualizar una lámina", description = "Si no se envía cantidad, se conserva la actual.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Lámina actualizada"),
			@ApiResponse(responseCode = "400", description = "Datos inválidos"),
			@ApiResponse(responseCode = "404", description = "La lámina no existe"),
			@ApiResponse(responseCode = "409", description = "Número fuera de rango o ya usado en el álbum") })
	@PutMapping("/laminas/{id}")
	public LaminaResponse actualizar(@PathVariable Long id, @Valid @RequestBody LaminaRequest request) {
		return laminaService.actualizar(id, request);
	}

	/**
	 * DELETE /api/laminas/{id}: elimina una lamina.
	 *
	 * @param id identificador
	 * @return 204 sin contenido
	 */
	@Operation(summary = "Eliminar una lámina", description = "También borra su foto, si tiene.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Lámina eliminada"),
			@ApiResponse(responseCode = "404", description = "La lámina no existe") })
	@DeleteMapping("/laminas/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		laminaService.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
