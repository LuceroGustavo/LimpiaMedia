package com.lucero.limpiamedia.controller;

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
import com.lucero.limpiamedia.model.ScanType;
import com.lucero.limpiamedia.model.ScannedFile;
import com.lucero.limpiamedia.repository.DuplicateGroupRepository;
import com.lucero.limpiamedia.repository.ScanSessionRepository;
import com.lucero.limpiamedia.service.ScanService;

@Controller
public class ScanController {

	private final ScanService scanService;
	private final ScanSessionRepository sessionRepo;
	private final DuplicateGroupRepository groupRepo;

	public ScanController(ScanService scanService, ScanSessionRepository sessionRepo,
			DuplicateGroupRepository groupRepo) {
		this.scanService = scanService;
		this.sessionRepo = sessionRepo;
		this.groupRepo = groupRepo;
	}

	@GetMapping("/scan/{tipo}")
	public String scan(@PathVariable ScanType tipo, Model model) {
		model.addAttribute("tipo", tipo);
		return "scan";
	}

	@PostMapping("/scan/{tipo}/iniciar")
	public String iniciar(@PathVariable ScanType tipo, @RequestParam String ruta, RedirectAttributes ra) {
		try {
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
	public String resultado(@PathVariable Long id, Model model) {
		ScanSession sesion = sessionRepo.findById(id).orElseThrow();
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

		model.addAttribute("sesion", sesion);
		model.addAttribute("grupos", grupos);
		model.addAttribute("totalDuplicados", totalDuplicados);
		model.addAttribute("totalEspacio", totalEspacio);
		return "resultado";
	}
}
