package com.lucero.limpiamedia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.lucero.limpiamedia.model.ScanType;

@Controller
public class ScanController {

	@GetMapping("/scan/{tipo}")
	public String scan(@PathVariable ScanType tipo, Model model) {
		model.addAttribute("tipo", tipo);
		return "scan";
	}
}
