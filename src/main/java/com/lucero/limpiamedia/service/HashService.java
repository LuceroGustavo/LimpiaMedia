package com.lucero.limpiamedia.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

import org.springframework.stereotype.Service;

@Service
public class HashService {

	public String sha256(Path ruta) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			try (InputStream in = Files.newInputStream(ruta)) {
				byte[] buffer = new byte[8192];
				int leidos;
				while ((leidos = in.read(buffer)) != -1) {
					md.update(buffer, 0, leidos);
				}
			}
			return HexFormat.of().formatHex(md.digest());
		} catch (Exception e) {
			return null;
		}
	}
}
