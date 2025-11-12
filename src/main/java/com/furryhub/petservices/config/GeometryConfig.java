package com.furryhub.petservices.config;


import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeometryConfig {

    private static final int SRID = 4326; // WGS 84 (used by most GPS coordinates)

    @Bean
    public GeometryFactory geometryFactory() {
        // Create a GeometryFactory with the chosen SRID
        return new GeometryFactory(new PrecisionModel(), SRID);
    }
}