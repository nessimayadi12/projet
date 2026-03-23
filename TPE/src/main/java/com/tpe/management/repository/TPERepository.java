package com.tpe.management.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class TPERepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Vérifie si un TPE existe et récupère ses informations
     * SELECT N_affiliation, N_compte FROM TPE WHERE N_AFFILIATION = ?
     */
    public Map<String, Object> findByAffiliation(String nAffiliation) {
        String sql = "SELECT N_affiliation, N_compte FROM TPE WHERE N_AFFILIATION = ?";
        
        try {
            return jdbcTemplate.queryForMap(sql, nAffiliation);
        } catch (Exception e) {
            return null;
        }
    }
}
