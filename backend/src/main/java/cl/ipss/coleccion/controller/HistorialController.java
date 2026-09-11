package cl.ipss.coleccion.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.ipss.coleccion.dto.AlbumResponse;
import cl.ipss.coleccion.dto.LaminaResponse;
import cl.ipss.coleccion.dto.RegistroHistorial;
import cl.ipss.coleccion.service.HistorialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints de auditoria: historial de cambios de albumes y laminas (Hibernate Envers).
 * Cada cambio registra revision, fecha, usuario ({@code X-Usuario}), operacion y datos.
 */
@Tag(name = "5. Auditoría", description = "Historial de cambios (Hibernate Envers), incluso de registros eliminados")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HistorialController {

	private final HistorialService historialService;

	/**
	 * GET /api/albumes/{id}/historial: versiones del album, incluso si fue eliminado.
	 *
	 * @param id album
	 * @return 200 con las revisiones, o 404 si nunca existio
	 */
	@Operation(summary = "Historial de un álbum", description = "Revisiones con fecha, usuario, operación (CREACION, MODIFICACION, ELIMINACION) y datos.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Revisiones ordenadas de la más antigua a la más reciente"),
			@ApiResponse(responseCode = "404", description = "Nunca existió un álbum con ese id") })
	@GetMapping("/albumes/{id}/historial")
	public List<RegistroHistorial<AlbumResponse>> historialAlbum(@PathVariable Long id) {
		return historialService.historialAlbum(id);
	}

	/**
	 * GET /api/laminas/{id}/historial: versiones de la lamina, incluso si fue eliminada.
	 *
	 * @param id lamina
	 * @return 200 con las revisiones, o 404 si nunca existio
	 */
	@Operation(summary = "Historial de una lámina", description = "Revisiones con fecha, usuario, operación y datos; incluye la eliminación en cascada del álbum.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Revisiones ordenadas de la más antigua a la más reciente"),
			@ApiResponse(responseCode = "404", description = "Nunca existió una lámina con ese id") })
	@GetMapping("/laminas/{id}/historial")
	public List<RegistroHistorial<LaminaResponse>> historialLamina(@PathVariable Long id) {
		return historialService.historialLamina(id);
	}
}
