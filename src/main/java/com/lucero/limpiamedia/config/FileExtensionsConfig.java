package com.lucero.limpiamedia.config;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.lucero.limpiamedia.model.ScanType;

@Component
public class FileExtensionsConfig {

	private final Map<ScanType, Set<String>> extensiones = new EnumMap<>(ScanType.class);

	public FileExtensionsConfig() {
		extensiones.put(ScanType.FOTOS, Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "tiff", "heic",
				"raw", "cr2", "nef", "jfif", "svg"));
		extensiones.put(ScanType.VIDEOS, Set.of("mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "m4v",
				"mpeg", "mpg", "3gp"));
		extensiones.put(ScanType.DOCUMENTOS, Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
				"txt", "rtf", "csv", "odt", "ods"));
		extensiones.put(ScanType.SONIDO, Set.of("mp3", "wav", "flac", "aac", "ogg", "m4a", "wma"));
	}

	public boolean tieneExtension(ScanType tipo, String nombreArchivo) {
		int i = nombreArchivo.lastIndexOf('.');
		if (i < 0) {
			return false;
		}
		String ext = nombreArchivo.substring(i + 1).toLowerCase(Locale.ROOT);
		return extensiones.getOrDefault(tipo, Set.of()).contains(ext);
	}

	public static String extension(String nombreArchivo) {
		int i = nombreArchivo.lastIndexOf('.');
		return i < 0 ? "" : nombreArchivo.substring(i + 1).toLowerCase(Locale.ROOT);
	}
}
