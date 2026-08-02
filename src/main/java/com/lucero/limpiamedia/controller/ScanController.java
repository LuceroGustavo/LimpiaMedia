package com.lucero.limpiamedia.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lucero.limpiamedia.config.FileExtensionsConfig;
import com.lucero.limpiamedia.model.DuplicateGroup;
import com.lucero.limpiamedia.model.ScanSession;
import com.lucero.limpiamedia.model.ScanStatus;
import com.lucero.limpiamedia.model.ScanType;
import com.lucero.limpiamedia.model.ScannedFile;
import com.lucero.limpiamedia.repository.DuplicateGroupRepository;
import com.lucero.limpiamedia.repository.ScanSessionRepository;
import com.lucero.limpiamedia.repository.ScannedFileRepository;
import com.lucero.limpiamedia.service.MoveService;
import com.lucero.limpiamedia.service.ScanService;

@Controller
public class ScanController {

	private final ScanService scanService;
	private final ScanSessionRepository sessionRepo;
	private final DuplicateGroupRepository groupRepo;
	private final ScannedFileRepository fileRepo;
	private final MoveService moveService;
	private final FileExtensionsConfig extConfig;

	public ScanController(ScanService scanService, ScanSessionRepository sessionRepo,
			DuplicateGroupRepository groupRepo, ScannedFileRepository fileRepo, MoveService moveService,
			FileExtensionsConfig extConfig) {
		this.scanService = scanService;
		this.sessionRepo = sessionRepo;
		this.groupRepo = groupRepo;
		this.fileRepo = fileRepo;
		this.moveService = moveService;
		this.extConfig = extConfig;
	}

	@GetMapping("/scan/{tipo}")
	public String scan(@PathVariable ScanType tipo, Model model) {
		model.addAttribute("tipo", tipo);
		return "scan";
	}

	@GetMapping("/scan/personalizado")
	public String scanPersonalizado(Model model) {
		model.addAttribute("categorias", extConfig.categorias());
		return "scan-personalizado";
	}

	@PostMapping("/scan/{tipo}/iniciar")
	public String iniciar(@PathVariable ScanType tipo, @RequestParam String ruta, RedirectAttributes ra) {
		try {
			if (hayEscaneoEnCurso(ra, "redirect:/scan/" + tipo)) {
				return "redirect:/scan/" + tipo;
			}
			ScanSession sesion = scanService.iniciarEscaneo(tipo, ruta);
			return "redirect:/escaneo/" + sesion.getId();
		} catch (IllegalArgumentException e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/scan/" + tipo;
		}
	}

	@PostMapping("/scan/personalizado/iniciar")
	public String iniciarPersonalizado(@RequestParam(name = "ext", required = false) List<String> extensiones,
			@RequestParam String ruta, RedirectAttributes ra) {
		try {
			if (hayEscaneoEnCurso(ra, "redirect:/scan/personalizado")) {
				return "redirect:/scan/personalizado";
			}
			Set<String> filtro = new HashSet<>(extensiones == null ? new ArrayList<>() : extensiones);
			ScanSession sesion = scanService.iniciarEscaneoPersonalizado(filtro, ruta);
			return "redirect:/escaneo/" + sesion.getId();
		} catch (IllegalArgumentException e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/scan/personalizado";
		}
	}

	private boolean hayEscaneoEnCurso(RedirectAttributes ra, String volverA) {
		List<ScanSession> enCurso = sessionRepo.findByEstadoOrderByInicioDesc(ScanStatus.EN_PROGRESO);
		if (!enCurso.isEmpty()) {
			ra.addFlashAttribute("error", "Ya hay un escaneo en curso (sesión " + enCurso.get(0).getId()
					+ "). Esperá a que termine antes de iniciar otro.");
			return true;
		}
		return false;
	}

	@GetMapping("/escaneo/{id}")
	public String progreso(@PathVariable Long id, Model model) {
		model.addAttribute("id", id);
		return "progreso";
	}

	@GetMapping("/escaneo/{id}/resultado")
	public String resultado(@PathVariable Long id, Model model, RedirectAttributes ra) {
		ScanSession sesion = sessionRepo.findById(id).orElseThrow();
		if (sesion.getEstado() == ScanStatus.EN_PROGRESO) {
			return "redirect:/escaneo/" + id;
		}
		if (sesion.getEstado() == ScanStatus.ERROR) {
			ra.addFlashAttribute("error", "El escaneo terminó con un error. Probá de nuevo.");
			return "redirect:" + urlScan(sesion);
		}
		List<DuplicateGroup> grupos = groupRepo.findBySesion_IdOrderById(id);

		long totalDuplicados = 0;
		long totalEspacio = 0;
		for (DuplicateGroup g : grupos) {
			for (ScannedFile f : g.getArchivos()) {
				if (f.isEsDuplicado()) {
					totalDuplicados++;
					totalEspacio += f.getTamanio();
				}
			}
		}

		String sugerencia = sugerirDestino(sesion);

		model.addAttribute("sesion", sesion);
		model.addAttribute("grupos", grupos);
		model.addAttribute("totalDuplicados", totalDuplicados);
		model.addAttribute("totalEspacio", totalEspacio);
		model.addAttribute("sugerencia", sugerencia);
		model.addAttribute("tituloTipo", tituloTipo(sesion));
		model.addAttribute("urlScan", urlScan(sesion));
		return "resultado";
	}

	private String urlScan(ScanSession sesion) {
		return sesion.getTipo() == ScanType.PERSONALIZADO ? "/scan/personalizado" : "/scan/" + sesion.getTipo();
	}

	private String tituloTipo(ScanSession sesion) {
		if (sesion.getTipo() == ScanType.PERSONALIZADO) {
			return "formatos " + sesion.getExtensionesFiltradas();
		}
		return sesion.getTipo().name();
	}

	@PostMapping("/escaneo/{id}/mover")
	public String mover(@PathVariable Long id, @RequestParam String carpetaDestino, RedirectAttributes ra) {
		try {
			int movidos = moveService.moverDuplicados(id, carpetaDestino);
			ra.addFlashAttribute("movidos", movidos);
			ra.addFlashAttribute("destino", carpetaDestino.trim());
		} catch (Exception e) {
			ra.addFlashAttribute("error", "No se pudieron mover los archivos: " + e.getMessage());
		}
		return "redirect:/escaneo/" + id + "/resultado";
	}

	@GetMapping("/escaneo/{id}/abrir-carpeta")
	public String abrirCarpeta(@PathVariable Long id, RedirectAttributes ra) {
		try {
			ScanSession sesion = sessionRepo.findById(id).orElseThrow(
					() -> new IllegalArgumentException("Sesión de escaneo no encontrada"));
			if (sesion.getCarpetaDestino() == null) {
				ra.addFlashAttribute("error", "Todavía no se movieron archivos en esta sesión");
				return "redirect:/escaneo/" + id + "/resultado";
			}
			new ProcessBuilder("explorer.exe", sesion.getCarpetaDestino()).start();
			ra.addFlashAttribute("ok", "Se abrió la carpeta destino en el Explorador");
		} catch (Exception e) {
			ra.addFlashAttribute("error", "No se pudo abrir la carpeta: " + e.getMessage());
		}
		return "redirect:/escaneo/" + id + "/resultado";
	}

	@GetMapping("/escaneo/{id}/exportar")
	public ResponseEntity<byte[]> exportar(@PathVariable Long id) throws IOException {
		ScanSession sesion = sessionRepo.findById(id).orElseThrow();
		List<DuplicateGroup> grupos = groupRepo.findBySesion_IdOrderById(id);

		StringBuilder csv = new StringBuilder();
		csv.append("\uFEFF"); // BOM UTF-8 para que Excel lea bien los acentos
		csv.append("Grupo;Etiqueta;Nombre;Ruta;Tamaño (MB);Hash\r\n");
		int nroGrupo = 0;
		for (DuplicateGroup g : grupos) {
			nroGrupo++;
			for (ScannedFile f : g.getArchivos()) {
				csv.append(nroGrupo).append(';');
				csv.append(f.isMovido() ? "MOVIDO" : (f.isEsDuplicado() ? "DUPLICADO" : "ORIGINAL")).append(';');
				csv.append(escaparCsv(f.getNombre())).append(';');
				csv.append(escaparCsv(f.getRuta())).append(';');
				csv.append(String.format(Locale.ROOT, "%.2f", f.getTamanio() / 1048576.0)).append(';');
				csv.append(f.getHash() == null ? "" : f.getHash()).append("\r\n");
			}
		}

		String nombreArchivo = "limpiamedia_escaneo_" + id + ".csv";
		String contentDisposition = "attachment; filename*=UTF-8''"
				+ URLEncoder.encode(nombreArchivo, StandardCharsets.UTF_8).replace("+", "%20");
		return ResponseEntity.ok()
				.header("Content-Disposition", contentDisposition)
				.contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
				.body(csv.toString().getBytes(StandardCharsets.UTF_8));
	}

	private String escaparCsv(String valor) {
		if (valor == null) {
			return "";
		}
		if (valor.contains(";") || valor.contains("\"") || valor.contains("\r") || valor.contains("\n")) {
			return "\"" + valor.replace("\"", "\"\"") + "\"";
		}
		return valor;
	}

	private static final Set<String> IMAGENES_PREVISIBLES = Set.of(
			"jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico", "jfif");

	@GetMapping("/archivo/{id}")
	public ResponseEntity<byte[]> imagen(@PathVariable Long id) throws IOException {
		ScannedFile sf = fileRepo.findById(id).orElse(null);
		if (sf == null) {
			return ResponseEntity.notFound().build();
		}
		String ext = sf.getExtension() == null ? "" : sf.getExtension().toLowerCase(Locale.ROOT);
		if (!IMAGENES_PREVISIBLES.contains(ext)) {
			return ResponseEntity.badRequest().build();
		}
		Path ruta = Paths.get(sf.getRuta());
		if (!Files.exists(ruta)) {
			return ResponseEntity.notFound().build();
		}
		MediaType tipo = mediaTypeImagen(ext);
		if (tipo == null) {
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok()
				.contentType(tipo)
				.body(Files.readAllBytes(ruta));
	}

	private MediaType mediaTypeImagen(String ext) {
		return switch (ext) {
			case "jpg", "jpeg", "jfif" -> MediaType.IMAGE_JPEG;
			case "png" -> MediaType.IMAGE_PNG;
			case "gif" -> MediaType.IMAGE_GIF;
			case "bmp" -> MediaType.parseMediaType("image/bmp");
			case "webp" -> MediaType.parseMediaType("image/webp");
			case "svg" -> MediaType.parseMediaType("image/svg+xml");
			case "ico" -> MediaType.parseMediaType("image/x-icon");
			default -> null;
		};
	}

	private String sugerirDestino(ScanSession sesion) {
		return Path.of(System.getProperty("user.home"), "Desktop",
				"LimpiaMedia_Duplicados_" + sesion.getTipo()).toString();
	}
}
