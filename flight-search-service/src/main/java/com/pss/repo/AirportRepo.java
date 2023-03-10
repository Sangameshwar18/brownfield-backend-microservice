package com.pss.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pss.entity.Airport;

@Repository
public interface AirportRepo extends JpaRepository<Airport, String> {

	public List<Airport> findAllByOrderByCodeAsc();
}
