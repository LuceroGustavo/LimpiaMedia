package com.lucero.limpiamedia.service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.lucero.limpiamedia.dto.CarpetaDTO;

@Service
public class FileSystemService {

	public List<CarpetaDTO> listarUnidades() {
		List<CarpetaDTO> unidades = new ArrayList<>();
		for (Path raiz : FileSystems.getDefault().getRootDirectories()) {
			unidades.add(new CarpetaDTO(raiz.toString(), raiz.toString()));
		}
		return unidades;
	}

	public List<CarpetaDTO> listarSubcarpetas(String ruta) {
		List<CarpetaDTO> carpetas = new ArrayList<>();
		if (ruta == null || ruta.isBlank()) {
			return carpetas;
		}
		Path path = Paths.get(ruta);
		if (!Files.isDirectory(path)) {
			return carpetas;
		}
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			for (Path p : stream) {
				if (Files.isDirectory(p)) {
					String nombre = p.getFileName() == null ? p.toString() : p.getFileName().toString();
					carpetas.add(new CarpetaDTO(p.toString(), nombre));
				}
			}
		} catch (IOException e) {
			return new ArrayList<>();
		}
		carpetas.sort(Comparator.comparing(CarpetaDTO::getNombre, String.CASE_INSENSITIVE_ORDER));
		return carpetas;
	}

	public boolean esDirectorio(String ruta) {
		if (ruta == null || ruta.isBlank()) {
			return false;
		}
		return Files.isDirectory(Paths.get(ruta));
	}
}
