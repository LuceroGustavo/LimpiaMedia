package com.lucero.limpiamedia.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lucero.limpiamedia.model.ScannedFile;

public interface ScannedFileRepository extends JpaRepository<ScannedFile, Long> {
}
