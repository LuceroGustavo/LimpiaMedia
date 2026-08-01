package com.lucero.limpiamedia.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lucero.limpiamedia.dto.CarpetaDTO;
import com.lucero.limpiamedia.service.FileSystemService;

@RestController
@RequestMapping("/api")
public class FolderController {

	private final FileSystemService fileSystemService;

	public FolderController(FileSystemService fileSystemService) {
		this.fileSystemService = fileSystemService;
	}

	@GetMapping("/unidades")
	public List<CarpetaDTO> unidades() {
		return fileSystemService.listarUnidades();
	}

	@GetMapping("/carpetas")
	public List<CarpetaDTO> carpetas(@RequestParam("ruta") String ruta) {
		return fileSystemService.listarSubcarpetas(ruta);
	}
}
