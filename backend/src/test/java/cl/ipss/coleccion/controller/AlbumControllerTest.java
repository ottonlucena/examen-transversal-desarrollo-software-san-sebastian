package cl.ipss.coleccion.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cl.ipss.coleccion.dto.AlbumResponse;
import cl.ipss.coleccion.entity.TipoLaminasAlbum;
import cl.ipss.coleccion.exception.RecursoNoEncontradoException;
import cl.ipss.coleccion.service.AlbumService;

/**
 * Pruebas de la capa web de {@link AlbumController} (MockMvc, sin base de datos): codigos HTTP,
 * header Location, validaciones de entrada y formato de error ProblemDetail.
 */
@WebMvcTest(AlbumController.class)
class AlbumControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AlbumService albumService;

	@Test
	void crearDevuelve201ConLocation() throws Exception {
		when(albumService.crear(any())).thenReturn(new AlbumResponse(1L, "Qatar 2022", null,
				LocalDate.of(2022, 8, 24), TipoLaminasAlbum.ADHESIVA, 20, null, null, null));

		mockMvc.perform(post("/api/albumes").contentType(MediaType.APPLICATION_JSON).content("""
				{"nombre":"Qatar 2022","fechaLanzamiento":"2022-08-24","tipoLaminas":"ADHESIVA","totalLaminas":20}
				"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", endsWith("/api/albumes/1")))
				.andExpect(jsonPath("$.nombre").value("Qatar 2022"));
	}

	@Test
	void crearConDatosInvalidosDevuelve400ConErroresPorCampo() throws Exception {
		mockMvc.perform(post("/api/albumes").contentType(MediaType.APPLICATION_JSON).content("""
				{"nombre":" ","fechaLanzamiento":"2999-01-01","tipoLaminas":"ADHESIVA","totalLaminas":0}
				"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Datos inválidos"))
				.andExpect(jsonPath("$.errores.nombre").exists())
				.andExpect(jsonPath("$.errores.fechaLanzamiento").exists())
				.andExpect(jsonPath("$.errores.totalLaminas").exists());
		verifyNoInteractions(albumService);
	}

	@Test
	void obtenerInexistenteDevuelve404ProblemDetail() throws Exception {
		when(albumService.obtener(99L)).thenThrow(new RecursoNoEncontradoException("No existe un álbum con id 99"));

		mockMvc.perform(get("/api/albumes/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Recurso no encontrado"))
				.andExpect(jsonPath("$.detail").value("No existe un álbum con id 99"));
	}
}
