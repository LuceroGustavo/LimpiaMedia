package com.lucero.limpiamedia.controller;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.lucero.limpiamedia.model.ScanSession;
import com.lucero.limpiamedia.repository.ScanSessionRepository;

@Controller
public class HomeController {

	private final ScanSessionRepository sessionRepo;

	public HomeController(ScanSessionRepository sessionRepo) {
		this.sessionRepo = sessionRepo;
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
