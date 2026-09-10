package com.freightfox.tollplaza.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.freightfox.tollplaza.entity.PincodeEntity;

@Repository
public interface PincodeRepository extends JpaRepository<PincodeEntity, String> {

    Optional<PincodeEntity> findByPincode(String pincode);
}
