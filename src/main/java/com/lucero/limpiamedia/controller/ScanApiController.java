package com.lucero.limpiamedia.controller;

import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lucero.limpiamedia.model.ScanSession;
import com.lucero.limpiamedia.repository.ScanSessionRepository;

@RestController
@RequestMapping("/api/escaneo")
public class ScanApiController {

	private final ScanSessionRepository sessionRepo;

	public ScanApiController(ScanSessionRepository sessionRepo) {
		this.sessionRepo = sessionRepo;
	}

	@GetMapping("/{id}")
	public Map<String, Object> estado(@PathVariable Long id) {
		ScanSession s = sessionRepo.findById(id).orElseThrow();
		Map<String, Object> respuesta = new LinkedHashMap<>();
		respuesta.put("id", s.getId());
		respuesta.put("estado", s.getEstado().name());
		respuesta.put("fase", s.getFase() == null ? "" : s.getFase().name());
		respuesta.put("total", s.getTotalArchivos());
		respuesta.put("procesados", s.getProcesados());
		respuesta.put("duplicados", s.getDuplicados());
		respuesta.put("totalVerificar", s.getTotalVerificar() == null ? 0 : s.getTotalVerificar());
		respuesta.put("procesadosVerificar", s.getProcesadosVerificar() == null ? 0 : s.getProcesadosVerificar());
		respuesta.put("inicioEpoch",
				s.getInicio() == null ? 0 : s.getInicio().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		return respuesta;
	}
}
