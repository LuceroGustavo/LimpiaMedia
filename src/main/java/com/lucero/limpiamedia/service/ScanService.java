package com.lucero.limpiamedia.service;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.lucero.limpiamedia.config.FileExtensionsConfig;
import com.lucero.limpiamedia.model.CarpetaExcluida;
import com.lucero.limpiamedia.model.DuplicateGroup;
import com.lucero.limpiamedia.model.ScanPhase;
import com.lucero.limpiamedia.model.ScanSession;
import com.lucero.limpiamedia.model.ScanStatus;
import com.lucero.limpiamedia.model.ScanType;
import com.lucero.limpiamedia.model.ScannedFile;
import com.lucero.limpiamedia.repository.CarpetaExcluidaRepository;
import com.lucero.limpiamedia.repository.DuplicateGroupRepository;
import com.lucero.limpiamedia.repository.ScanSessionRepository;

@Service
public class ScanService {

	private static final Logger log = LoggerFactory.getLogger(ScanService.class);
	private static final int GUARDAR_CADA = 500;
	private static final int GUARDAR_HASH_CADA = 100;

	private final ScanSessionRepository sessionRepo;
	private final DuplicateGroupRepository groupRepo;
	private final CarpetaExcluidaRepository exclRepo;
	private final FileExtensionsConfig extConfig;
	private final HashService hashService;
	private final ThreadPoolTaskExecutor scanExecutor;

	public ScanService(ScanSessionRepository sessionRepo, DuplicateGroupRepository groupRepo,
			CarpetaExcluidaRepository exclRepo, FileExtensionsConfig extConfig, HashService hashService,
			@Qualifier("scanExecutor") ThreadPoolTaskExecutor scanExecutor) {
		this.sessionRepo = sessionRepo;
		this.groupRepo = groupRepo;
		this.exclRepo = exclRepo;
		this.extConfig = extConfig;
		this.hashService = hashService;
		this.scanExecutor = scanExecutor;
	}

	public ScanSession iniciarEscaneo(ScanType tipo, String ruta) {
		return iniciarEscaneoCon(tipo, extConfig.extensionesDe(tipo), ruta);
	}

	public ScanSession iniciarEscaneoPersonalizado(Set<String> extensiones, String ruta) {
		if (extensiones == null || extensiones.isEmpty()) {
			throw new IllegalArgumentException("Tenés que seleccionar al menos un formato.");
		}
		return iniciarEscaneoCon(ScanType.PERSONALIZADO, new HashSet<>(extensiones), ruta);
	}

	private ScanSession iniciarEscaneoCon(ScanType tipo, Set<String> extensiones, String ruta) {
		Path raiz = Paths.get(ruta);
		if (!Files.isDirectory(raiz)) {
			throw new IllegalArgumentException("La ruta no es una carpeta válida: " + ruta);
		}
		ScanSession sesion = new ScanSession();
		sesion.setTipo(tipo);
		if (tipo == ScanType.PERSONALIZADO) {
			sesion.setExtensionesFiltradas(extensiones.stream().sorted().collect(Collectors.joining(", ")));
		}
		sesion.setRutaRaiz(ruta);
		sesion.setEstado(ScanStatus.EN_PROGRESO);
		sesion.setInicio(LocalDateTime.now());
		sesion = sessionRepo.save(sesion);

		final Long id = sesion.getId();
		scanExecutor.execute(() -> ejecutarEscaneo(id, extensiones, raiz));
		return sesion;
	}

	private void ejecutarEscaneo(Long id, Set<String> extensiones, Path raiz) {
		ScanSession sesion = sessionRepo.findById(id).orElse(null);
		if (sesion == null) {
			return;
		}
		try {
			List<Path> excluidas = cargarExcluidas(raiz);

			sesion.setFase(ScanPhase.CONTANDO);
			long total = contarArchivos(extensiones, raiz, excluidas);
			sesion.setTotalArchivos(total);
			sesion.setFase(ScanPhase.RECOLECTANDO);
			sesion = sessionRepo.save(sesion);

			List<InfoArchivo> archivos = new ArrayList<>();
			recolectarArchivos(extensiones, raiz, archivos, sesion, excluidas);
			sesion.setFase(ScanPhase.VERIFICANDO);
			sesion = sessionRepo.save(sesion);

			List<DuplicateGroup> grupos = detectarDuplicados(archivos, sesion);

			long duplicados = grupos.stream()
					.flatMap(g -> g.getArchivos().stream())
					.filter(ScannedFile::isEsDuplicado)
					.count();
			sesion.setProcesados(sesion.getTotalArchivos());
			sesion.setProcesadosVerificar(sesion.getTotalVerificar());
			sesion.setDuplicados(duplicados);
			sesion.setEstado(ScanStatus.COMPLETADO);
			sesion.setFin(LocalDateTime.now());
			sesion = sessionRepo.save(sesion);

			groupRepo.saveAll(grupos);
			log.info("Escaneo {} completado: {} archivos, {} duplicados en {} grupos",
					id, sesion.getTotalArchivos(), duplicados, grupos.size());
		} catch (Exception e) {
			log.error("Error en el escaneo " + id, e);
			sesion.setEstado(ScanStatus.ERROR);
			sesion.setFin(LocalDateTime.now());
			sessionRepo.save(sesion);
		}
	}

	private List<Path> cargarExcluidas(Path raiz) {
		return exclRepo.findAll().stream()
				.map(c -> Paths.get(c.getRutaExcluida()).toAbsolutePath().normalize())
				.toList();
	}

	private boolean estaExcluida(Path dir, List<Path> excluidas) {
		Path actual = dir.toAbsolutePath().normalize();
		for (Path ex : excluidas) {
			if (actual.startsWith(ex)) {
				return true;
			}
		}
		return false;
	}

	private long contarArchivos(Set<String> extensiones, Path raiz, List<Path> excluidas) throws IOException {
		long[] contador = { 0 };
		Files.walkFileTree(raiz, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
				if (estaExcluida(dir, excluidas)) {
					return FileVisitResult.SKIP_SUBTREE;
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				if (extConfig.tieneExtension(extensiones, file.getFileName().toString())) {
					contador[0]++;
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) {
				return FileVisitResult.CONTINUE;
			}
		});
		return contador[0];
	}

	private void recolectarArchivos(Set<String> extensiones, Path raiz, List<InfoArchivo> destino, ScanSession sesion,
			List<Path> excluidas) throws IOException {
		Files.walkFileTree(raiz, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
				if (estaExcluida(dir, excluidas)) {
					return FileVisitResult.SKIP_SUBTREE;
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				String nombre = file.getFileName().toString();
				if (extConfig.tieneExtension(extensiones, nombre)) {
					Path padre = file.getParent();
					destino.add(new InfoArchivo(nombre, file.toString(),
							padre == null ? "" : padre.toString(),
							FileExtensionsConfig.extension(nombre), attrs.size()));
					sesion.setProcesados(sesion.getProcesados() + 1);
					if (sesion.getProcesados() % GUARDAR_CADA == 0) {
						sessionRepo.save(sesion);
					}
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) {
				return FileVisitResult.CONTINUE;
			}
		});
		sessionRepo.save(sesion);
	}

	private List<DuplicateGroup> detectarDuplicados(List<InfoArchivo> archivos, ScanSession sesion) {
		Set<String> nombresPresentes = archivos.stream().map(InfoArchivo::nombre).collect(Collectors.toSet());
		Map<InfoArchivo, String> normalizados = new HashMap<>();
		for (InfoArchivo a : archivos) {
			normalizados.put(a, normalizarNombre(a.nombre(), nombresPresentes));
		}

		Map<String, List<InfoArchivo>> porNombreTamanio = new HashMap<>();
		for (InfoArchivo a : archivos) {
			porNombreTamanio
					.computeIfAbsent(normalizados.get(a) + "|" + a.tamanio(), k -> new ArrayList<>())
					.add(a);
		}

		List<DuplicateGroup> grupos = new ArrayList<>();
		long totalVerificar = porNombreTamanio.values().stream()
				.filter(c -> c.size() >= 2)
				.mapToLong(List::size)
				.sum();
		sesion.setTotalVerificar(totalVerificar);
		sesion.setProcesadosVerificar(0L);
		sessionRepo.save(sesion);

		int hasheados = 0;

		for (List<InfoArchivo> candidatos : porNombreTamanio.values()) {
			if (candidatos.size() < 2) {
				continue;
			}
			Map<String, List<InfoArchivo>> porHash = new HashMap<>();
			Map<String, String> hashPorRuta = new HashMap<>();
			for (InfoArchivo a : candidatos) {
				String hash = hashService.sha256(Paths.get(a.ruta()));
				if (hash == null) {
					continue;
				}
				hashPorRuta.put(a.ruta(), hash);
				porHash.computeIfAbsent(hash, k -> new ArrayList<>()).add(a);
				sesion.setProcesadosVerificar(sesion.getProcesadosVerificar() + 1);
				if (++hasheados % GUARDAR_HASH_CADA == 0) {
					sessionRepo.save(sesion);
				}
			}

			for (List<InfoArchivo> iguales : porHash.values()) {
				if (iguales.size() < 2) {
					continue;
				}
				iguales.sort(Comparator
						.comparingInt((InfoArchivo a) -> a.nombre().equals(normalizados.get(a)) ? 0 : 1)
						.thenComparing(a -> a.nombre()));
				InfoArchivo primero = iguales.get(0);
				DuplicateGroup grupo = new DuplicateGroup();
				grupo.setSesion(sesion);
				grupo.setClave(normalizados.get(primero) + "|" + primero.tamanio());
				grupo.setHash(hashPorRuta.get(primero.ruta()));
				grupo.setTamanio(primero.tamanio());
				for (int i = 0; i < iguales.size(); i++) {
					InfoArchivo ia = iguales.get(i);
					ScannedFile sf = new ScannedFile();
					sf.setSesion(sesion);
					sf.setGrupo(grupo);
					sf.setNombre(ia.nombre());
					sf.setRuta(ia.ruta());
					sf.setCarpetaPadre(ia.carpetaPadre());
					sf.setExtension(ia.extension());
					sf.setTamanio(ia.tamanio());
					sf.setHash(hashPorRuta.get(ia.ruta()));
					sf.setEsDuplicado(i > 0);
					grupo.getArchivos().add(sf);
				}
				grupos.add(grupo);
			}
		}
		sessionRepo.save(sesion);
		return grupos;
	}

	private record InfoArchivo(String nombre, String ruta, String carpetaPadre, String extension, long tamanio) {
	}

	/**
	 * Normaliza el nombre para agrupar copias de Windows como duplicados.
	 * Los marcadores de texto inequívocos ("copia de ", " - copia", " (2)")
	 * se quitan siempre; el sufijo numérico "_1"/"_2" solo se quita si el
	 * nombre base resultante existe entre los archivos presentes (el copiado
	 * convive con el original), así no se tocan timestamps como
	 * "VID_20260105_161551289.mp4". El hash SHA-256 confirma el contenido.
	 */
	static String normalizarNombre(String nombre, Set<String> nombresPresentes) {
		int dot = nombre.lastIndexOf('.');
		String base = dot < 0 ? nombre : nombre.substring(0, dot);
		String ext = dot < 0 ? "" : nombre.substring(dot);

		String s = base;
		String anterior;
		do {
			anterior = s;
			s = s.replaceFirst("(?i)^(copia de |copy of |copia_|copy_)", "");
			s = s.replaceFirst("(?i)( - (copia|copy))( \\(\\d+\\))?$", "");
			s = s.replaceFirst("(?i) \\(\\d+\\)$", "");
		} while (!s.equals(anterior));

		while (true) {
			String sinNumero = s.replaceFirst("_\\d+$", "");
			if (sinNumero.equals(s) || !nombresPresentes.contains(sinNumero + ext)) {
				break;
			}
			s = sinNumero;
		}
		return s + ext;
	}
}
