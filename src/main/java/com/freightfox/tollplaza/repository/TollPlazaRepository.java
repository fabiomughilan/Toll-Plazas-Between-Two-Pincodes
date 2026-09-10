package com.freightfox.tollplaza.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.freightfox.tollplaza.entity.TollPlazaEntity;

@Repository
public interface TollPlazaRepository extends JpaRepository<TollPlazaEntity, Long> {

    Optional<TollPlazaEntity> findByName(String name);
}
