package com.lucero.limpiamedia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lucero.limpiamedia.repository.MoveRecordRepository;
import com.lucero.limpiamedia.service.MoveService;

@Controller
public class MoveController {

	private final MoveService moveService;
	private final MoveRecordRepository moveRepo;

	public MoveController(MoveService moveService, MoveRecordRepository moveRepo) {
		this.moveService = moveService;
		this.moveRepo = moveRepo;
	}

	@GetMapping("/movimientos")
	public String movimientos(Model model) {
		model.addAttribute("movimientos", moveRepo.findAllByOrderByFechaDesc());
		return "movimientos";
	}

	@PostMapping("/movimientos/{id}/restaurar")
	public String restaurar(@PathVariable Long id, RedirectAttributes ra) {
		try {
			moveService.restaurar(id);
			ra.addFlashAttribute("ok", "Archivo restaurado a su ubicación original.");
		} catch (Exception e) {
			ra.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/movimientos";
	}
}
