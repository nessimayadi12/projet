package com.banque.abc.tpe.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@DisplayName("Execution SQL securisee de l'assistant IA")
class SqlExecutorServiceTest {

    @Test
    @DisplayName("Refuse une requete non SELECT")
    void refuseRequeteNonSelect() {
        SqlExecutorService service = new SqlExecutorService(mock(JdbcTemplate.class));

        assertThrows(SecurityException.class, () -> service.executer("DELETE FROM tpes"));
    }

    @Test
    @DisplayName("Refuse une requete SELECT contenant une instruction dangereuse")
    void refuseInstructionDangereuse() {
        SqlExecutorService service = new SqlExecutorService(mock(JdbcTemplate.class));

        assertThrows(SecurityException.class, () -> service.executer("SELECT * FROM tpes; DROP TABLE tpes"));
    }
}
