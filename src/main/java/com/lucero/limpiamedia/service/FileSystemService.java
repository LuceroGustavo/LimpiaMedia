package com.lucero.limpiamedia.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lucero.limpiamedia.dto.CarpetaDTO;

@Service
public class FileSystemService {

	private static final Logger log = LoggerFactory.getLogger(FileSystemService.class);
	private static final long TIMEOUT_RED_SEGUNDOS = 3;
	private static final ExecutorService RED_THREADS = Executors.newFixedThreadPool(2,
			runnable -> {
				Thread t = new Thread(runnable, "red-fs");
				t.setDaemon(true);
				return t;
			});

	public List<CarpetaDTO> listarUnidades() {
		List<CarpetaDTO> unidades = new ArrayList<>();
		for (Path raiz : FileSystems.getDefault().getRootDirectories()) {
			unidades.add(new CarpetaDTO(raiz.toString(), raiz.toString()));
		}
		return unidades;
	}

	public List<CarpetaDTO> listarServidores() {
		List<CarpetaDTO> servidores = new ArrayList<>();
		String[] lineas = ejecutarConTimeout("net", "view");
		for (String linea : lineas) {
			linea = linea.trim();
			if (!linea.startsWith("\\\\")) {
				continue;
			}
			int fin = linea.indexOf(' ');
			String servidor = fin < 0 ? linea : linea.substring(0, fin);
			servidores.add(new CarpetaDTO(servidor, servidor));
		}
		return servidores;
	}

	public List<CarpetaDTO> listarSubcarpetas(String ruta) {
		if (ruta == null || ruta.isBlank()) {
			return new ArrayList<>();
		}
		if (esRutaRed(ruta)) {
			if (esServidorSinCompartido(ruta)) {
				return listarCompartidosDe(extraerServidor(ruta));
			}
			return conTimeout(() -> listarSubcarpetasLocal(ruta), new ArrayList<>());
		}
		return listarSubcarpetasLocal(ruta);
	}

	private List<CarpetaDTO> listarSubcarpetasLocal(String ruta) {
		List<CarpetaDTO> carpetas = new ArrayList<>();
		Path path = Paths.get(ruta);
		if (!Files.isDirectory(path)) {
			return carpetas;
		}
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			for (Path p : stream) {
				if (Files.isDirectory(p) && !esCarpetaOculta(p)) {
					String nombre = p.getFileName() == null ? p.toString() : p.getFileName().toString();
					carpetas.add(new CarpetaDTO(p.toString(), nombre));
				}
			}
		} catch (IOException e) {
			return new ArrayList<>();
		}
		carpetas.sort(Comparator.comparing(CarpetaDTO::getNombre, String.CASE_INSENSITIVE_ORDER));
		return carpetas;
	}

	private List<CarpetaDTO> listarCompartidosDe(String servidor) {
		List<CarpetaDTO> compartidos = new ArrayList<>();
		String[] lineas = ejecutarConTimeout("net", "view", servidor);
		boolean cuerpo = false;
		for (String linea : lineas) {
			linea = linea.trim();
			if (linea.isEmpty()) {
				continue;
			}
			if (!cuerpo) {
				if (linea.matches("^[\\-\\=]{3,}$")) {
					cuerpo = true;
				}
				continue;
			}
			String nombre = linea.split("\\s+")[0];
			if (nombre.isEmpty() || nombre.contains("$")) {
				continue;
			}
			compartidos.add(new CarpetaDTO(servidor + "\\" + nombre, nombre));
		}
		return compartidos;
	}

	private String[] ejecutarConTimeout(String... comando) {
		try {
			Process proceso = new ProcessBuilder(comando).redirectErrorStream(true).start();
			if (!proceso.waitFor(TIMEOUT_RED_SEGUNDOS, TimeUnit.SECONDS)) {
				proceso.destroyForcibly();
				log.warn("El comando {} no respondió en {} s", comando[0], TIMEOUT_RED_SEGUNDOS);
				return new String[0];
			}
			try (BufferedReader lector = new BufferedReader(
					new InputStreamReader(proceso.getInputStream(), Charset.defaultCharset()))) {
				return lector.lines().toArray(String[]::new);
			}
		} catch (Exception e) {
			log.warn("No se pudo ejecutar {}: {}", comando[0], e.getMessage());
			return new String[0];
		}
	}

	private <T> T conTimeout(Callable<T> tarea, T porDefecto) {
		Future<T> futuro = RED_THREADS.submit(tarea);
		try {
			return futuro.get(TIMEOUT_RED_SEGUNDOS, TimeUnit.SECONDS);
		} catch (Exception e) {
			futuro.cancel(true);
			return porDefecto;
		}
	}

	private boolean esRutaRed(String ruta) {
		return ruta.startsWith("\\\\");
	}

	private boolean esServidorSinCompartido(String ruta) {
		String resto = normalizarSeparadores(ruta);
		if (resto.isEmpty()) {
			return false;
		}
		return resto.split("/").length <= 1;
	}

	private String extraerServidor(String ruta) {
		String resto = normalizarSeparadores(ruta);
		int barra = resto.indexOf('/');
		String servidor = barra < 0 ? resto : resto.substring(0, barra);
		return "\\\\" + servidor;
	}

	private String normalizarSeparadores(String ruta) {
		return ruta.replace('\\', '/').replaceAll("^/+", "").replaceAll("/+$", "");
	}

	private boolean esCarpetaOculta(Path p) {
		String nombre = p.getFileName() == null ? "" : p.getFileName().toString();
		if (nombre.startsWith("$") || nombre.startsWith(".")) {
			return true;
		}
		try {
			DosFileAttributes attrs = Files.readAttributes(p, DosFileAttributes.class);
			return attrs.isHidden() || attrs.isSystem();
		} catch (IOException e) {
			return false;
		}
	}

	public boolean esDirectorio(String ruta) {
		if (ruta == null || ruta.isBlank()) {
			return false;
		}
		return Files.isDirectory(Paths.get(ruta));
	}
}
