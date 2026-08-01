package com.lucero.limpiamedia.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lucero.limpiamedia.model.DuplicateGroup;

public interface DuplicateGroupRepository extends JpaRepository<DuplicateGroup, Long> {
}
