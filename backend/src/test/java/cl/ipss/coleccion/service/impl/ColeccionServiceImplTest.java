package cl.ipss.coleccion.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.ipss.coleccion.dto.CantidadRequest;
import cl.ipss.coleccion.dto.FaltantesResponse;
import cl.ipss.coleccion.dto.LaminaRequest;
import cl.ipss.coleccion.dto.LaminaResponse;
import cl.ipss.coleccion.dto.LoteLaminasRequest;
import cl.ipss.coleccion.dto.RegistroLaminasRequest;
import cl.ipss.coleccion.dto.RegistroLaminasResponse;
import cl.ipss.coleccion.dto.RepetidasResponse;
import cl.ipss.coleccion.dto.ResumenAlbumResponse;
import cl.ipss.coleccion.entity.Album;
import cl.ipss.coleccion.entity.EstadoLamina;
import cl.ipss.coleccion.entity.Lamina;
import cl.ipss.coleccion.entity.TipoLamina;
import cl.ipss.coleccion.exception.ReglaNegocioException;
import cl.ipss.coleccion.repository.AlbumRepository;
import cl.ipss.coleccion.repository.EstadisticasAlbum;
import cl.ipss.coleccion.repository.LaminaRepository;

/**
 * Pruebas unitarias de la logica de negocio de {@link ColeccionServiceImpl}, con los
 * repositorios simulados (Mockito): faltantes, repetidas, carga en lote, registro, cantidad y resumen.
 */
@ExtendWith(MockitoExtension.class)
class ColeccionServiceImplTest {

	private static final Long ALBUM_ID = 1L;

	@Mock
	private AlbumRepository albumRepository;

	@Mock
	private LaminaRepository laminaRepository;

	@InjectMocks
	private ColeccionServiceImpl servicio;

	@Test
	void faltantesIncluyeLaminasSinCopiasYNumerosNoCatalogados() {
		Album album = album(5);
		when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album));
		when(laminaRepository.findByAlbumIdAndCantidadOrderByNumeroAsc(ALBUM_ID, 0))
				.thenReturn(List.of(lamina(album, 2, 0)));
		when(laminaRepository.findNumerosByAlbumId(ALBUM_ID)).thenReturn(List.of(1, 2, 3));

		FaltantesResponse faltantes = servicio.faltantes(ALBUM_ID);

		assertThat(faltantes.laminas()).extracting(LaminaResponse::numero).containsExactly(2);
		assertThat(faltantes.numerosNoCatalogados()).containsExactly(4, 5);
		assertThat(faltantes.totalFaltantes()).isEqualTo(3);
	}

	@Test
	void repetidasIndicaCopiasSobrantesPorLaminaYTotal() {
		Album album = album(10);
		when(albumRepository.existsById(ALBUM_ID)).thenReturn(true);
		when(laminaRepository.findByAlbumIdAndCantidadGreaterThanOrderByNumeroAsc(ALBUM_ID, 1))
				.thenReturn(List.of(lamina(album, 3, 3), lamina(album, 5, 2)));

		RepetidasResponse repetidas = servicio.repetidas(ALBUM_ID);

		assertThat(repetidas.laminas()).extracting(LaminaResponse::repetidas).containsExactly(2, 1);
		assertThat(repetidas.laminasRepetidas()).isEqualTo(2);
		assertThat(repetidas.totalCopiasRepetidas()).isEqualTo(3);
	}

	@Test
	void loteConConflictosInformaCadaPosicionYNoGuardaNada() {
		when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album(10)));
		when(laminaRepository.findNumerosByAlbumId(ALBUM_ID)).thenReturn(List.of(8));
		LoteLaminasRequest lote = new LoteLaminasRequest(List.of(
				new LaminaRequest(8, "Existente", TipoLamina.NORMAL, null),
				new LaminaRequest(11, "Fuera de rango", TipoLamina.NORMAL, null),
				new LaminaRequest(9, "Valida", TipoLamina.NORMAL, null)));

		assertThatThrownBy(() -> servicio.cargarLote(ALBUM_ID, lote))
				.isInstanceOf(ReglaNegocioException.class)
				.satisfies(ex -> assertThat(((ReglaNegocioException) ex).getErrores())
						.containsOnlyKeys("laminas[0].numero", "laminas[1].numero"));
		verify(laminaRepository, never()).saveAll(any());
	}

	@Test
	void registrarSumaUnaCopiaPorCadaAparicion() {
		Album album = album(10);
		Lamina uno = lamina(album, 1, 0);
		Lamina dos = lamina(album, 2, 1);
		when(albumRepository.existsById(ALBUM_ID)).thenReturn(true);
		when(laminaRepository.findByAlbumIdAndNumeroIn(ALBUM_ID, Set.of(1, 2))).thenReturn(List.of(uno, dos));

		RegistroLaminasResponse respuesta = servicio.registrarObtenidas(ALBUM_ID,
				new RegistroLaminasRequest(List.of(1, 2, 2)));

		assertThat(respuesta.copiasRegistradas()).isEqualTo(3);
		assertThat(uno.getCantidad()).isEqualTo(1);
		assertThat(dos.getCantidad()).isEqualTo(3);
		assertThat(dos.getEstado()).isEqualTo(EstadoLamina.REPETIDA);
	}

	@Test
	void registrarRechazaNumerosNoCatalogadosSinModificarNada() {
		Album album = album(10);
		Lamina uno = lamina(album, 1, 0);
		when(albumRepository.existsById(ALBUM_ID)).thenReturn(true);
		when(laminaRepository.findByAlbumIdAndNumeroIn(ALBUM_ID, Set.of(1, 99))).thenReturn(List.of(uno));

		assertThatThrownBy(() -> servicio.registrarObtenidas(ALBUM_ID, new RegistroLaminasRequest(List.of(1, 99))))
				.isInstanceOf(ReglaNegocioException.class)
				.satisfies(ex -> assertThat(((ReglaNegocioException) ex).getErrores().get("numeros")).contains("99"));
		assertThat(uno.getCantidad()).isZero();
	}

	@Test
	void ajustarCantidadNoPermiteQuedarNegativa() {
		Lamina lamina = lamina(album(10), 4, 0);
		when(laminaRepository.findById(lamina.getId())).thenReturn(Optional.of(lamina));

		assertThatThrownBy(() -> servicio.ajustarCantidad(lamina.getId(), new CantidadRequest(-1)))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("negativa");
		assertThat(lamina.getCantidad()).isZero();
	}

	@Test
	void resumenCalculaFaltantesYPorcentajeSobreElTotalDelAlbum() {
		when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album(20)));
		when(laminaRepository.calcularEstadisticas(ALBUM_ID)).thenReturn(new EstadisticasAlbum(18L, 5L, 2L, 3L));

		ResumenAlbumResponse resumen = servicio.resumen(ALBUM_ID);

		assertThat(resumen.catalogadas()).isEqualTo(18);
		assertThat(resumen.obtenidas()).isEqualTo(5);
		assertThat(resumen.faltantes()).isEqualTo(15);
		assertThat(resumen.copiasRepetidas()).isEqualTo(3);
		assertThat(resumen.porcentajeCompletado()).isEqualTo(25.0);
	}

	private static Album album(int totalLaminas) {
		Album album = new Album();
		album.setId(ALBUM_ID);
		album.setNombre("Album de prueba");
		album.setTotalLaminas(totalLaminas);
		return album;
	}

	private static Lamina lamina(Album album, int numero, int cantidad) {
		Lamina lamina = new Lamina();
		lamina.setId(100L + numero);
		lamina.setAlbum(album);
		lamina.setNumero(numero);
		lamina.setNombre("Lamina " + numero);
		lamina.setTipo(TipoLamina.NORMAL);
		lamina.setCantidad(cantidad);
		return lamina;
	}
}
