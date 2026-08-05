package com.lucero.limpiamedia.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
	public String abrirCarpeta(@RequestParam String ruta, @RequestParam String volver, @RequestParam String patron,
			RedirectAttributes ra) {
		Path archivo = Paths.get(ruta);
		if (!Files.exists(archivo)) {
			ra.addFlashAttribute("error", "El archivo ya no existe: " + ruta);
		} else {
			Path carpeta = Files.isDirectory(archivo) ? archivo : archivo.getParent();
			try {
				new ProcessBuilder("explorer.exe", carpeta.toString()).start();
				ra.addFlashAttribute("ok", "Se abrió la carpeta en el Explorador");
			} catch (Exception e) {
				ra.addFlashAttribute("error", "No se pudo abrir la carpeta: " + e.getMessage());
			}
		}
		return "redirect:/comparativas/resultado?ruta=" + codificar(volver) + "&patron=" + codificar(patron);
	}

	private String codificar(String valor) {
		return URLEncoder.encode(valor, StandardCharsets.UTF_8).replace("+", "%20");
	}
}
