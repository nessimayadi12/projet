package com.tpe.management.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class PorteurRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Vérifie si une carte porteur existe et récupère ses informations avec devise
     * SELECT ncarte, compte, devise, ccy_id, ccy_rate, deci_places
     * FROM PORTEUR p
     * LEFT JOIN FM_CURRENCY fc ON p.devise = fc.ccy_id
     * LEFT JOIN RATES r ON fc.ccy_id = r.ccy_id
     * WHERE p.ncarte = ?
     */
    public Map<String, Object> findByNumeroCarteWithCurrency(String ncarte) {
        String sql = "SELECT p.ncarte, p.compte, p.devise, " +
                     "fc.ccy_id, ISNULL(r.ccy_rate, 1.0) as ccy_rate, " +
                     "ISNULL(fc.deci_places, 3) as deci_places " +
                     "FROM PORTEUR p " +
                     "LEFT JOIN FM_CURRENCY fc ON p.devise = fc.ccy_id " +
                     "LEFT JOIN RATES r ON fc.ccy_id = r.ccy_id " +
                     "WHERE p.ncarte = ?";
        
        try {
            return jdbcTemplate.queryForMap(sql, ncarte);
        } catch (Exception e) {
            return null;
        }
    }
}
