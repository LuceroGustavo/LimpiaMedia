package com.lucero.limpiamedia.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.lucero.limpiamedia.model.ScanSession;
import com.lucero.limpiamedia.model.ScanStatus;
import com.lucero.limpiamedia.repository.ScanSessionRepository;

@Component
public class LimpiezaSesionesListener {

	private static final Logger log = LoggerFactory.getLogger(LimpiezaSesionesListener.class);

	private final ScanSessionRepository sessionRepo;

	public LimpiezaSesionesListener(ScanSessionRepository sessionRepo) {
		this.sessionRepo = sessionRepo;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void marcarSesionesHuerfanas() {
		List<ScanSession> colgadas = sessionRepo.findByEstadoOrderByInicioDesc(ScanStatus.EN_PROGRESO);
		for (ScanSession s : colgadas) {
			s.setEstado(ScanStatus.ERROR);
			sessionRepo.save(s);
			log.info("Sesión {} quedó sin terminar (reinicio); marcada como ERROR", s.getId());
		}
	}
}
