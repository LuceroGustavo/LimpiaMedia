package com.lucero.limpiamedia.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lucero.limpiamedia.config.FileExtensionsConfig;
import com.lucero.limpiamedia.dto.FileComparativaDTO;
import com.lucero.limpiamedia.service.ComparativaService;

@Controller
public class ComparativaController {

	private final ComparativaService comparativaService;

	public ComparativaController(ComparativaService comparativaService) {
		this.comparativaService = comparativaService;
	}

	@GetMapping("/comparativas")
	public String formulario() {
		return "comparativas";
	}

	@PostMapping("/comparativas/buscar")
	public String buscar(@RequestParam String ruta, @RequestParam String patron, RedirectAttributes ra) {
		if (ruta == null || ruta.isBlank() || !Files.isDirectory(Paths.get(ruta))) {
			ra.addFlashAttribute("error", "Elegí una carpeta o disco válido para buscar.");
			return "redirect:/comparativas";
		}
		String p = patron == null ? "" : patron.trim();
		if (p.isEmpty()) {
			ra.addFlashAttribute("error", "Indicá un patrón de búsqueda (ej: *.xlsx, *avances*).");
			return "redirect:/comparativas";
		}
		return "redirect:/comparativas/resultado?ruta=" + codificar(ruta) + "&patron=" + codificar(p);
	}

	@GetMapping("/comparativas/resultado")
	public String resultado(@RequestParam String ruta, @RequestParam String patron, Model model) {
		try {
			List<FileComparativaDTO> resultados = comparativaService.buscar(ruta, patron);
			long totalBytes = resultados.stream().mapToLong(FileComparativaDTO::getTamanio).sum();
			model.addAttribute("ruta", ruta);
			model.addAttribute("patron", patron);
			model.addAttribute("resultados", resultados);
			model.addAttribute("total", resultados.size());
			model.addAttribute("totalBytes", totalBytes);
		} catch (IllegalArgumentException e) {
			model.addAttribute("error", e.getMessage());
		}
		return "comparativas";
	}

	@GetMapping("/comparativas/abrir-carpeta")
	public ResponseEntity<Map<String, String>> abrirCarpeta(@RequestParam String ruta) {
		Path archivo = Paths.get(ruta);
		if (!Files.exists(archivo)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("ok", "false", "mensaje", "El archivo ya no existe: " + ruta));
		}
		Path carpeta = Files.isDirectory(archivo) ? archivo : archivo.getParent();
		try {
			new ProcessBuilder("explorer.exe", carpeta.toString()).start();
			return ResponseEntity.ok(Map.of("ok", "true", "mensaje", "Se abrió la carpeta en el Explorador"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("ok", "false", "mensaje", "No se pudo abrir la carpeta: " + e.getMessage()));
		}
	}

	@GetMapping("/comparativas/miniatura")
	public ResponseEntity<byte[]> miniatura(@RequestParam String ruta) {
		Path archivo = Paths.get(ruta);
		if (!Files.isRegularFile(archivo)) {
			return ResponseEntity.notFound().build();
		}
		String ext = FileExtensionsConfig.extension(archivo.getFileName().toString());
		if (!IMAGENES_PREVISIBLES.contains(ext)) {
			return ResponseEntity.badRequest().build();
		}
		MediaType tipo = mediaTypeImagen(ext);
		if (tipo == null) {
			return ResponseEntity.badRequest().build();
		}
		try {
			return ResponseEntity.ok()
					.contentType(tipo)
					.body(Files.readAllBytes(archivo));
		} catch (IOException e) {
			return ResponseEntity.notFound().build();
		}
	}

	private static final Set<String> IMAGENES_PREVISIBLES = Set.of(
			"jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico", "jfif");

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

	private String codificar(String valor) {
		return URLEncoder.encode(valor, StandardCharsets.UTF_8).replace("+", "%20");
	}
}
