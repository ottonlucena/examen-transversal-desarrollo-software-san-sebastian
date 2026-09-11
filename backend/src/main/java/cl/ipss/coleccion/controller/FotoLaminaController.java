package cl.ipss.coleccion.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cl.ipss.coleccion.dto.ArchivoFoto;
import cl.ipss.coleccion.dto.LaminaResponse;
import cl.ipss.coleccion.service.LaminaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints de la foto opcional de cada lamina. La foto se recibe por
 * {@code multipart/form-data} en el campo {@code archivo} (JPEG, PNG o WEBP, maximo 5 MB).
 */
@Tag(name = "4. Fotos de láminas", description = "Foto opcional de cada lámina (multipart/form-data, campo 'archivo')")
@RestController
@RequestMapping("/api/laminas/{id}/foto")
@RequiredArgsConstructor
public class FotoLaminaController {

	private final LaminaService laminaService;

	/**
	 * POST /api/laminas/{id}/foto: sube o reemplaza la foto de la lamina.
	 *
	 * @param id      lamina
	 * @param archivo imagen en el campo {@code archivo}
	 * @return 200 con la lamina y su {@code fotoUrl}
	 */
	@Operation(summary = "Subir o reemplazar la foto", description = "JPEG, PNG o WEBP de hasta 5 MB. El tipo se valida por el contenido real del archivo.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Foto guardada; la lámina incluye fotoUrl"),
			@ApiResponse(responseCode = "400", description = "Falta el campo 'archivo'"),
			@ApiResponse(responseCode = "404", description = "La lámina no existe"),
			@ApiResponse(responseCode = "413", description = "El archivo supera 5 MB"),
			@ApiResponse(responseCode = "415", description = "Archivo vacío, no es imagen permitida o la petición no es multipart") })
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public LaminaResponse subir(@PathVariable Long id, @RequestParam("archivo") MultipartFile archivo) {
		return laminaService.subirFoto(id, archivo);
	}

	/**
	 * GET /api/laminas/{id}/foto: devuelve la imagen con su Content-Type.
	 *
	 * @param id lamina
	 * @return 200 con la imagen, o 404 si la lamina no tiene foto
	 */
	@Operation(summary = "Ver la foto")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Imagen de la lámina", content = {
					@Content(mediaType = MediaType.IMAGE_JPEG_VALUE),
					@Content(mediaType = MediaType.IMAGE_PNG_VALUE),
					@Content(mediaType = "image/webp") }),
			@ApiResponse(responseCode = "404", description = "La lámina no existe o no tiene foto") })
	@GetMapping
	public ResponseEntity<Resource> ver(@PathVariable Long id) {
		ArchivoFoto foto = laminaService.obtenerFoto(id);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(foto.tipoContenido()))
				.body(foto.contenido());
	}

	/**
	 * DELETE /api/laminas/{id}/foto: quita la foto de la lamina y borra el archivo.
	 *
	 * @param id lamina
	 * @return 204 sin contenido, o 404 si no tenia foto
	 */
	@Operation(summary = "Quitar la foto", description = "Borra el archivo del disco al confirmar la transacción.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Foto eliminada"),
			@ApiResponse(responseCode = "404", description = "La lámina no existe o no tiene foto") })
	@DeleteMapping
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		laminaService.eliminarFoto(id);
		return ResponseEntity.noContent().build();
	}
}
