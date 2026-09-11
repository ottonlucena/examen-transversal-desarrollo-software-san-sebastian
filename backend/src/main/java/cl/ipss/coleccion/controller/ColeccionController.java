package cl.ipss.coleccion.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.ipss.coleccion.dto.CantidadRequest;
import cl.ipss.coleccion.dto.FaltantesResponse;
import cl.ipss.coleccion.dto.LaminaResponse;
import cl.ipss.coleccion.dto.LoteLaminasRequest;
import cl.ipss.coleccion.dto.RegistroLaminasRequest;
import cl.ipss.coleccion.dto.RegistroLaminasResponse;
import cl.ipss.coleccion.dto.RepetidasResponse;
import cl.ipss.coleccion.dto.ResumenAlbumResponse;
import cl.ipss.coleccion.service.ColeccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints de las funcionalidades especiales para el manejo de laminas: carga masiva,
 * registro de obtenidas, ajuste de cantidad, faltantes, repetidas y resumen del album.
 */
@Tag(name = "3. Colección", description = "Carga masiva, registro de obtenidas, faltantes, repetidas y resumen")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ColeccionController {

	private final ColeccionService coleccionService;

	/**
	 * POST /api/albumes/{albumId}/laminas/lote: carga masiva de laminas (todo o nada).
	 *
	 * @param albumId album destino
	 * @param request listado de laminas
	 * @return 201 con las laminas creadas
	 */
	@Operation(summary = "Cargar un listado de láminas", description = "Todo o nada: si alguna lámina tiene conflictos no se crea ninguna y se informa cada posición.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Láminas creadas"),
			@ApiResponse(responseCode = "400", description = "Lote vacío, datos inválidos o números repetidos dentro del lote"),
			@ApiResponse(responseCode = "404", description = "El álbum no existe"),
			@ApiResponse(responseCode = "409", description = "Números fuera de rango o ya existentes (detalle por posición)") })
	@PostMapping("/albumes/{albumId}/laminas/lote")
	public ResponseEntity<List<LaminaResponse>> cargarLote(@PathVariable Long albumId,
			@Valid @RequestBody LoteLaminasRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(coleccionService.cargarLote(albumId, request));
	}

	/**
	 * POST /api/albumes/{albumId}/laminas/registrar: suma una copia por cada numero recibido.
	 *
	 * @param albumId album
	 * @param request numeros obtenidos, por ejemplo {@code {"numeros":[1,5,5]}}
	 * @return 200 con las laminas afectadas
	 */
	@Operation(summary = "Registrar láminas obtenidas", description = "Cada aparición de un número suma una copia: [1,5,5] suma 1 a la lámina 1 y 2 a la 5. Todo o nada.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Copias registradas"),
			@ApiResponse(responseCode = "400", description = "Lista vacía o números inválidos"),
			@ApiResponse(responseCode = "404", description = "El álbum no existe"),
			@ApiResponse(responseCode = "409", description = "Números no catalogados o se supera el máximo de copias") })
	@PostMapping("/albumes/{albumId}/laminas/registrar")
	public RegistroLaminasResponse registrarObtenidas(@PathVariable Long albumId,
			@Valid @RequestBody RegistroLaminasRequest request) {
		return coleccionService.registrarObtenidas(albumId, request);
	}

	/**
	 * PATCH /api/laminas/{id}/cantidad: suma o resta copias, por ejemplo {@code {"delta":-1}}.
	 *
	 * @param id      lamina
	 * @param request delta a aplicar
	 * @return 200 con la lamina actualizada
	 */
	@Operation(summary = "Sumar o restar copias de una lámina", description = "La cantidad resultante debe quedar entre 0 y 999.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Cantidad actualizada"),
			@ApiResponse(responseCode = "400", description = "Delta faltante o fuera de rango"),
			@ApiResponse(responseCode = "404", description = "La lámina no existe"),
			@ApiResponse(responseCode = "409", description = "La cantidad quedaría negativa o sobre el máximo") })
	@PatchMapping("/laminas/{id}/cantidad")
	public LaminaResponse ajustarCantidad(@PathVariable Long id, @Valid @RequestBody CantidadRequest request) {
		return coleccionService.ajustarCantidad(id, request);
	}

	/**
	 * GET /api/albumes/{albumId}/laminas/faltantes: laminas que faltan para completar el album.
	 *
	 * @param albumId album
	 * @return 200 con las faltantes
	 */
	@Operation(summary = "Listar láminas faltantes", description = "Láminas con 0 copias y números del álbum que aún no se han catalogado.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Faltantes del álbum"),
			@ApiResponse(responseCode = "404", description = "El álbum no existe") })
	@GetMapping("/albumes/{albumId}/laminas/faltantes")
	public FaltantesResponse faltantes(@PathVariable Long albumId) {
		return coleccionService.faltantes(albumId);
	}

	/**
	 * GET /api/albumes/{albumId}/laminas/repetidas: laminas repetidas con la cantidad de repetidas de cada una.
	 *
	 * @param albumId album
	 * @return 200 con las repetidas
	 */
	@Operation(summary = "Listar láminas repetidas", description = "Cada lámina indica en 'repetidas' cuántas copias sobrantes hay.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Repetidas del álbum"),
			@ApiResponse(responseCode = "404", description = "El álbum no existe") })
	@GetMapping("/albumes/{albumId}/laminas/repetidas")
	public RepetidasResponse repetidas(@PathVariable Long albumId) {
		return coleccionService.repetidas(albumId);
	}

	/**
	 * GET /api/albumes/{albumId}/resumen: totales y porcentaje de avance del album.
	 *
	 * @param albumId album
	 * @return 200 con el resumen
	 */
	@Operation(summary = "Resumen del álbum", description = "Catalogadas, obtenidas, faltantes, repetidas y porcentaje completado (una sola consulta).")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Resumen del álbum"),
			@ApiResponse(responseCode = "404", description = "El álbum no existe") })
	@GetMapping("/albumes/{albumId}/resumen")
	public ResumenAlbumResponse resumen(@PathVariable Long albumId) {
		return coleccionService.resumen(albumId);
	}
}
