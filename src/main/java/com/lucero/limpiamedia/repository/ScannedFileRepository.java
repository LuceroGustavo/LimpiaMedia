package com.lucero.limpiamedia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lucero.limpiamedia.model.ScannedFile;

public interface ScannedFileRepository extends JpaRepository<ScannedFile, Long> {

	List<ScannedFile> findBySesionIdAndEsDuplicadoTrue(Long sesionId);
}
