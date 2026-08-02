package com.lucero.limpiamedia.controller;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lucero.limpiamedia.model.ScanSession;
import com.lucero.limpiamedia.repository.ScanSessionRepository;
import com.lucero.limpiamedia.service.ScanService;

@Controller
public class HomeController {

	private final ScanSessionRepository sessionRepo;
	private final ScanService scanService;

	public HomeController(ScanSessionRepository sessionRepo, ScanService scanService) {
		this.sessionRepo = sessionRepo;
		this.scanService = scanService;
	}

	@PostMapping("/escaneos/borrar")
	public String borrarHistorialEscaneos(RedirectAttributes ra) {
		int borradas = scanService.borrarHistorialEscaneos();
		ra.addFlashAttribute("ok", "Historial de escaneos borrado (" + borradas + " sesiones).");
		return "redirect:/";
	}

	@GetMapping("/")
	public String index(Model model) {
		List<ScanSession> sesiones = sessionRepo.findAll(Sort.by(Sort.Direction.DESC, "inicio"));

		long totalEscaneos = sesiones.size();
		long totalArchivos = sesiones.stream().mapToLong(ScanSession::getTotalArchivos).sum();
		long totalDuplicados = sesiones.stream().mapToLong(ScanSession::getDuplicados).sum();
		long completados = sesiones.stream()
				.filter(s -> s.getEstado().name().equals("COMPLETADO"))
				.count();

		model.addAttribute("sesiones", sesiones);
		model.addAttribute("totalEscaneos", totalEscaneos);
		model.addAttribute("totalArchivos", totalArchivos);
		model.addAttribute("totalDuplicados", totalDuplicados);
		model.addAttribute("completados", completados);
		return "index";
	}
}
