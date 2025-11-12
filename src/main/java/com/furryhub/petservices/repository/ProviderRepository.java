package com.furryhub.petservices.repository;

import com.furryhub.petservices.model.entity.Provider;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {

    @Query(value = """
        SELECT p.id,
               ST_Distance(
                   CAST(p.location AS geography),
                   CAST(ST_SetSRID(ST_MakePoint(:lon, :lat), 4326) AS geography)
               ) AS distance_m
        FROM providers p
        WHERE ST_DWithin(
               CAST(p.location AS geography),
               CAST(ST_SetSRID(ST_MakePoint(:lon, :lat), 4326) AS geography),
               :meters
        )
        ORDER BY distance_m
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findNearbyProvidersWithDistance(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("meters") double meters,
            @Param("limit") int limit);

    Optional<Provider> findByUser_Email(String email);
}

