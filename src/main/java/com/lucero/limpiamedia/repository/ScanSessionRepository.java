package com.lucero.limpiamedia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lucero.limpiamedia.model.ScanSession;
import com.lucero.limpiamedia.model.ScanStatus;

public interface ScanSessionRepository extends JpaRepository<ScanSession, Long> {

	List<ScanSession> findByEstadoOrderByInicioDesc(ScanStatus estado);
}
