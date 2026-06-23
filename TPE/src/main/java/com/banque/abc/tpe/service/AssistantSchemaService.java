package com.banque.abc.tpe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssistantSchemaService {

    private static final List<String> TABLES_AUTORISEES = List.of(
            "tpes",
            "commercants",
            "demandes",
            "affectations",
            "pannes",
            "users",
            "historique_statuts"
    );

    private static final String FALLBACK_SCHEMA = """
            tpes(id, numero_serie, numero_terminal, typetpe/type_tpe, statut,
                 marque, modele, date_acquisition, date_mise_en_service,
                 mcc, numero_affiliation, commercant_id, commentaire,
                 created_date, last_modified_date)
            commercants(id, raison_sociale, activite, numero_compte, adresse,
                        localite, code_postal, code_agence, telephone, email,
                        statut, loyer, type_commerce, url_site_marchand,
                        webmaster, contact_technique)
            demandes(id, reference, type_demande, statut, commercant_id,
                     demandeur_id, valideur_id, inputer_id, date_saisie_taux,
                     date_validation, date_cloture, description,
                     commentaire_validation, urgence, raison_sociale, activite,
                     numero_compte, adresse, code_postal, code_agence,
                     telephone, mcc, taux_commission, taux_commission_inter,
                     loyer, serie_tpe, numero_terminal, value_date,
                     localite, rib, webmaster, contact_technique,
                     url_site_marchand, created_date, last_modified_date)
            affectations(id, tpe_id, commercant_id, demande_id, date_affectation,
                         date_mise_en_service, date_fin, actif, commentaire,
                         affecte_par_id, created_date, last_modified_date)
            pannes(id, reference, tpe_id, statut, description, type_panne,
                   date_declaration, date_diagnostic, date_reparation,
                   date_resolution, declarant_id, technicien_id, diagnostic,
                   action_corrective, commentaire_technicien, tpe_remplacement_id,
                   cout_reparation, sous_garantie, created_date)
            users(id, username, nom, prenom, email, telephone, code_agence, actif)
            historique_statuts(id, tpe_id, ancien_statut, nouveau_statut,
                               date_changement, change_par, commentaire)
            """;

    private final JdbcTemplate jdbcTemplate;

    public String decrireSchema() {
        try {
            String placeholders = String.join(",", Collections.nCopies(TABLES_AUTORISEES.size(), "?"));
            String sql = "SELECT table_name, column_name "
                    + "FROM information_schema.columns "
                    + "WHERE table_schema = DATABASE() "
                    + "AND table_name IN (" + placeholders + ") "
                    + "ORDER BY table_name, ordinal_position";

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, TABLES_AUTORISEES.toArray());
            Map<String, List<String>> columnsByTable = new LinkedHashMap<>();
            for (String table : TABLES_AUTORISEES) {
                columnsByTable.put(table, new ArrayList<>());
            }

            for (Map<String, Object> row : rows) {
                String table = String.valueOf(readColumn(row, "table_name")).toLowerCase();
                String column = String.valueOf(readColumn(row, "column_name"));
                if (columnsByTable.containsKey(table)) {
                    columnsByTable.get(table).add(column);
                }
            }

            String schema = columnsByTable.entrySet().stream()
                    .filter(entry -> !entry.getValue().isEmpty())
                    .map(entry -> entry.getKey() + "(" + String.join(", ", entry.getValue()) + ")")
                    .collect(Collectors.joining("\n"));

            if (schema.isBlank()) {
                return FALLBACK_SCHEMA;
            }
            return schema;
        } catch (Exception ex) {
            log.warn("Schema SQL dynamique indisponible, utilisation du schema de secours", ex);
            return FALLBACK_SCHEMA;
        }
    }

    private Object readColumn(Map<String, Object> row, String name) {
        Object value = row.get(name);
        if (value != null) {
            return value;
        }
        return row.get(name.toUpperCase());
    }
}
