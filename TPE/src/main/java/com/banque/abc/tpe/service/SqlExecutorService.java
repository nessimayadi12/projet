package com.banque.abc.tpe.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@Slf4j
public class SqlExecutorService {

    private static final int MAX_ROWS = 200;
    private static final Pattern FORBIDDEN_SQL = Pattern.compile(
            "\\b(INSERT|UPDATE|DELETE|DROP|TRUNCATE|ALTER|CREATE|REPLACE|MERGE|GRANT|REVOKE|CALL|EXEC)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private final JdbcTemplate jdbcTemplate;

    public SqlExecutorService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Valide et execute un SELECT genere par l'IA, puis retourne les lignes.
     */
    public List<Map<String, Object>> executer(String sql) {
        String safeSql = validerEtLimiter(sql);
        try {
            return jdbcTemplate.queryForList(safeSql);
        } catch (Exception ex) {
            log.error("Erreur execution SQL IA: {}", safeSql, ex);
            throw new IllegalStateException(
                    "Impossible d'executer la requete generee par l'IA. Detail: " + rootCauseMessage(ex),
                    ex
            );
        }
    }

    /**
     * Refuse toute requete non SELECT et ajoute une limite si necessaire.
     */
    private String validerEtLimiter(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new SecurityException("SQL vide refuse");
        }

        String trimmed = stripTrailingSemicolon(sql.trim());
        String normalized = trimmed.toUpperCase(Locale.ROOT);
        if (!normalized.startsWith("SELECT")) {
            throw new SecurityException("SQL non autorise refuse");
        }
        if (FORBIDDEN_SQL.matcher(trimmed).find()) {
            throw new SecurityException("Instruction SQL dangereuse refusee");
        }
        if (trimmed.contains(";")) {
            throw new SecurityException("Requetes multiples refusees");
        }
        if (!normalized.matches("(?s).*\\bLIMIT\\b.*")) {
            return trimmed + " LIMIT " + MAX_ROWS;
        }
        return trimmed;
    }

    /**
     * Supprime uniquement le point-virgule final, pas les separateurs internes.
     */
    private String stripTrailingSemicolon(String sql) {
        String result = sql;
        while (result.endsWith(";")) {
            result = result.substring(0, result.length() - 1).trim();
        }
        return result;
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        Throwable last = throwable;
        while (current != null) {
            last = current;
            current = current.getCause();
        }
        return last.getMessage() != null ? last.getMessage() : last.getClass().getSimpleName();
    }
}
