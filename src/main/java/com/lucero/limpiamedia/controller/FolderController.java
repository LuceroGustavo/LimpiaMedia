package com.lucero.limpiamedia.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lucero.limpiamedia.dto.CarpetaDTO;
import com.lucero.limpiamedia.model.RutaReciente;
import com.lucero.limpiamedia.service.FileSystemService;
import com.lucero.limpiamedia.service.ScanService;

@RestController
@RequestMapping("/api")
public class FolderController {

	private final FileSystemService fileSystemService;
	private final ScanService scanService;

	public FolderController(FileSystemService fileSystemService, ScanService scanService) {
		this.fileSystemService = fileSystemService;
		this.scanService = scanService;
	}

	@GetMapping("/unidades")
	public List<CarpetaDTO> unidades() {
		return fileSystemService.listarUnidades();
	}

	@GetMapping("/red")
	public List<CarpetaDTO> red() {
		return fileSystemService.listarServidores();
	}

	@GetMapping("/recientes")
	public List<RutaReciente> recientes() {
		return scanService.listarRecientes();
	}

	@GetMapping("/carpetas")
	public List<CarpetaDTO> carpetas(@RequestParam("ruta") String ruta) {
		return fileSystemService.listarSubcarpetas(ruta);
	}
}
