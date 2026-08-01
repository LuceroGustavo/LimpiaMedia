package com.lucero.limpiamedia.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lucero.limpiamedia.model.DuplicateGroup;
import com.lucero.limpiamedia.model.ScanSession;
import com.lucero.limpiamedia.model.ScanStatus;
import com.lucero.limpiamedia.model.ScanType;
import com.lucero.limpiamedia.model.ScannedFile;
import com.lucero.limpiamedia.repository.DuplicateGroupRepository;
import com.lucero.limpiamedia.repository.ScanSessionRepository;
import com.lucero.limpiamedia.service.MoveService;
import com.lucero.limpiamedia.service.ScanService;

@Controller
public class ScanController {

	private final ScanService scanService;
	private final ScanSessionRepository sessionRepo;
	private final DuplicateGroupRepository groupRepo;
	private final MoveService moveService;

	public ScanController(ScanService scanService, ScanSessionRepository sessionRepo,
			DuplicateGroupRepository groupRepo, MoveService moveService) {
		this.scanService = scanService;
		this.sessionRepo = sessionRepo;
		this.groupRepo = groupRepo;
		this.moveService = moveService;
	}

	@GetMapping("/scan/{tipo}")
	public String scan(@PathVariable ScanType tipo, Model model) {
		model.addAttribute("tipo", tipo);
		return "scan";
	}

	@PostMapping("/scan/{tipo}/iniciar")
	public String iniciar(@PathVariable ScanType tipo, @RequestParam String ruta, RedirectAttributes ra) {
		try {
			List<ScanSession> enCurso = sessionRepo.findByEstadoOrderByInicioDesc(ScanStatus.EN_PROGRESO);
			if (!enCurso.isEmpty()) {
				ra.addFlashAttribute("error", "Ya hay un escaneo en curso (sesión " + enCurso.get(0).getId()
						+ "). Esperá a que termine antes de iniciar otro.");
				return "redirect:/scan/" + tipo;
			}
			ScanSession sesion = scanService.iniciarEscaneo(tipo, ruta);
			return "redirect:/escaneo/" + sesion.getId();
		} catch (IllegalArgumentException e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/scan/" + tipo;
		}
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
			return "redirect:/scan/" + sesion.getTipo();
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
		return "resultado";
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

	private String sugerirDestino(ScanSession sesion) {
		return Path.of(System.getProperty("user.home"), "Desktop",
				"LimpiaMedia_Duplicados_" + sesion.getTipo()).toString();
	}
}
