package com.lucero.limpiamedia.controller;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
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
	private final ConfigurableApplicationContext context;

	public HomeController(ScanSessionRepository sessionRepo, ScanService scanService,
			ConfigurableApplicationContext context) {
		this.sessionRepo = sessionRepo;
		this.scanService = scanService;
		this.context = context;
	}

	@PostMapping("/escaneos/borrar")
	public String borrarHistorialEscaneos(RedirectAttributes ra) {
		int borradas = scanService.borrarHistorialEscaneos();
		ra.addFlashAttribute("ok", "Historial de escaneos borrado (" + borradas + " sesiones).");
		return "redirect:/";
	}

	@PostMapping("/apagar")
	public String apagar() {
		new Thread(() -> {
			try {
				Thread.sleep(500);
			} catch (InterruptedException ignored) {
			}
			SpringApplication.exit(context, () -> 0);
		}, "apagar-limpiamedia").start();
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
