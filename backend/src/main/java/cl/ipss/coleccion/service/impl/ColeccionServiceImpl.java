package cl.ipss.coleccion.service.impl;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import cl.ipss.coleccion.entity.Lamina;
import cl.ipss.coleccion.exception.RecursoNoEncontradoException;
import cl.ipss.coleccion.exception.ReglaNegocioException;
import cl.ipss.coleccion.mapper.LaminaMapper;
import cl.ipss.coleccion.repository.AlbumRepository;
import cl.ipss.coleccion.repository.EstadisticasAlbum;
import cl.ipss.coleccion.repository.LaminaRepository;
import cl.ipss.coleccion.service.ColeccionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementacion de {@link ColeccionService}. Los filtros de faltantes y repetidas y los
 * totales del resumen se resuelven con consultas en la base de datos; las operaciones masivas
 * son transaccionales y validan todo el lote antes de escribir.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ColeccionServiceImpl implements ColeccionService {

	private final AlbumRepository albumRepository;
	private final LaminaRepository laminaRepository;

	@Override
	@Transactional
	public List<LaminaResponse> cargarLote(Long albumId, LoteLaminasRequest request) {
		Album album = buscarAlbum(albumId);
		Set<Integer> existentes = new HashSet<>(laminaRepository.findNumerosByAlbumId(albumId));
		List<LaminaRequest> items = request.laminas();

		Map<String, String> errores = new LinkedHashMap<>();
		for (int i = 0; i < items.size(); i++) {
			int numero = items.get(i).numero();
			String campo = "laminas[%d].numero".formatted(i);
			if (numero > album.getTotalLaminas()) {
				errores.put(campo, "El número %d excede el total de láminas del álbum (%d)"
						.formatted(numero, album.getTotalLaminas()));
			} else if (existentes.contains(numero)) {
				errores.put(campo, "El álbum ya tiene una lámina con el número " + numero);
			}
		}
		if (!errores.isEmpty()) {
			throw new ReglaNegocioException(
					"El lote no se cargó: %d lámina(s) con conflictos".formatted(errores.size()), errores);
		}

		List<Lamina> nuevas = items.stream().map(item -> LaminaMapper.toEntity(item, album)).toList();
		List<LaminaResponse> creadas = laminaRepository.saveAll(nuevas).stream()
				.sorted(Comparator.comparing(Lamina::getNumero))
				.map(LaminaMapper::toResponse)
				.toList();
		log.info("Lote cargado albumId={} laminas={}", albumId, creadas.size());
		return creadas;
	}

	@Override
	@Transactional
	public RegistroLaminasResponse registrarObtenidas(Long albumId, RegistroLaminasRequest request) {
		verificarAlbum(albumId);
		Map<Integer, Long> copiasPorNumero = request.numeros().stream()
				.collect(Collectors.groupingBy(Function.identity(), TreeMap::new, Collectors.counting()));
		Map<Integer, Lamina> laminas = laminaRepository.findByAlbumIdAndNumeroIn(albumId, copiasPorNumero.keySet())
				.stream().collect(Collectors.toMap(Lamina::getNumero, Function.identity()));

		List<Integer> noCatalogados = copiasPorNumero.keySet().stream()
				.filter(numero -> !laminas.containsKey(numero)).toList();
		if (!noCatalogados.isEmpty()) {
			throw new ReglaNegocioException("Hay números que no están catalogados en el álbum",
					Map.of("numeros", "No existen en el álbum: " + unir(noCatalogados)));
		}

		Map<String, String> excedidas = new LinkedHashMap<>();
		copiasPorNumero.forEach((numero, copias) -> {
			long total = laminas.get(numero).getCantidad() + copias;
			if (total > Lamina.CANTIDAD_MAXIMA) {
				excedidas.put("numeros[" + numero + "]", "La lámina %d quedaría con %d copias (máximo %d)"
						.formatted(numero, total, Lamina.CANTIDAD_MAXIMA));
			}
		});
		if (!excedidas.isEmpty()) {
			throw new ReglaNegocioException("Se supera el máximo de copias permitido", excedidas);
		}

		copiasPorNumero.forEach((numero, copias) -> {
			Lamina lamina = laminas.get(numero);
			lamina.setCantidad(lamina.getCantidad() + copias.intValue());
		});
		laminaRepository.flush(); // aplica la auditoria antes de responder
		log.info("Registradas {} copia(s) en albumId={} ({} lamina(s) distintas)",
				request.numeros().size(), albumId, copiasPorNumero.size());
		List<LaminaResponse> afectadas = copiasPorNumero.keySet().stream()
				.map(laminas::get).map(LaminaMapper::toResponse).toList();
		return new RegistroLaminasResponse(request.numeros().size(), afectadas);
	}

	@Override
	@Transactional
	public LaminaResponse ajustarCantidad(Long laminaId, CantidadRequest request) {
		Lamina lamina = laminaRepository.findById(laminaId)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe una lámina con id " + laminaId));
		int nueva = lamina.getCantidad() + request.delta();
		if (nueva < 0) {
			throw new ReglaNegocioException("La cantidad no puede quedar negativa: la lámina tiene %d copia(s)"
					.formatted(lamina.getCantidad()));
		}
		if (nueva > Lamina.CANTIDAD_MAXIMA) {
			throw new ReglaNegocioException("La cantidad no puede superar %d copias".formatted(Lamina.CANTIDAD_MAXIMA));
		}
		lamina.setCantidad(nueva);
		laminaRepository.saveAndFlush(lamina); // aplica la auditoria (modificadoEn/Por) antes de responder
		log.info("Cantidad ajustada laminaId={} delta={} nuevaCantidad={}", laminaId, request.delta(), nueva);
		return LaminaMapper.toResponse(lamina);
	}

	@Override
	public FaltantesResponse faltantes(Long albumId) {
		Album album = buscarAlbum(albumId);
		List<LaminaResponse> sinCopias = laminaRepository.findByAlbumIdAndCantidadOrderByNumeroAsc(albumId, 0)
				.stream().map(LaminaMapper::toResponse).toList();
		// Complemento del catalogo dentro del rango 1..totalLaminas (acotado por el total del album)
		Set<Integer> catalogados = new HashSet<>(laminaRepository.findNumerosByAlbumId(albumId));
		List<Integer> noCatalogados = IntStream.rangeClosed(1, album.getTotalLaminas())
				.filter(numero -> !catalogados.contains(numero)).boxed().toList();
		return new FaltantesResponse(albumId, album.getTotalLaminas(),
				sinCopias.size() + noCatalogados.size(), sinCopias, noCatalogados);
	}

	@Override
	public RepetidasResponse repetidas(Long albumId) {
		verificarAlbum(albumId);
		List<Lamina> repetidas = laminaRepository.findByAlbumIdAndCantidadGreaterThanOrderByNumeroAsc(albumId, 1);
		int copiasSobrantes = repetidas.stream().mapToInt(Lamina::getRepetidas).sum();
		return new RepetidasResponse(albumId, repetidas.size(), copiasSobrantes,
				repetidas.stream().map(LaminaMapper::toResponse).toList());
	}

	@Override
	public ResumenAlbumResponse resumen(Long albumId) {
		Album album = buscarAlbum(albumId);
		EstadisticasAlbum estadisticas = laminaRepository.calcularEstadisticas(albumId);
		int total = album.getTotalLaminas();
		int obtenidas = estadisticas.obtenidas().intValue();
		double porcentaje = Math.round(obtenidas * 1000.0 / total) / 10.0;
		return new ResumenAlbumResponse(albumId, album.getNombre(), total,
				estadisticas.catalogadas().intValue(), obtenidas, total - obtenidas,
				estadisticas.laminasRepetidas().intValue(), estadisticas.copiasRepetidas().intValue(), porcentaje);
	}

	private Album buscarAlbum(Long albumId) {
		return albumRepository.findById(albumId)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe un álbum con id " + albumId));
	}

	private void verificarAlbum(Long albumId) {
		if (!albumRepository.existsById(albumId)) {
			throw new RecursoNoEncontradoException("No existe un álbum con id " + albumId);
		}
	}

	private static String unir(List<Integer> numeros) {
		return numeros.stream().map(String::valueOf).collect(Collectors.joining(", "));
	}
}
