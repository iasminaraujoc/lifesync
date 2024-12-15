package br.com.lifesync.controller;

import br.com.lifesync.domain.compromisso.CompromissoDTO;
import br.com.lifesync.domain.compromisso.CompromissoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CompromissoControllerTest {

    @InjectMocks
    private CompromissoController compromissoController;

    @Mock
    private CompromissoService compromissoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listarCompromissos_DeveRetornarListaDeCompromissos() {
        // Arrange
        CompromissoDTO compromisso1 = new CompromissoDTO(1L, "Compromisso 1", "2024-12-15", "10:00", "tarefa");
        CompromissoDTO compromisso2 = new CompromissoDTO(2L, "Compromisso 2", "2024-12-16", "11:00", "evento");
        List<CompromissoDTO> compromissosMock = List.of(compromisso1, compromisso2);

        when(compromissoService.listarCompromissosOrdenados()).thenReturn(compromissosMock);

        // Act
        ResponseEntity<List<CompromissoDTO>> response = compromissoController.listarCompromissos();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(compromissosMock, response.getBody());
    }

    @Test
    void listarCompromissos_DeveRetornarListaVazia() {
        // Arrange
        when(compromissoService.listarCompromissosOrdenados()).thenReturn(List.of());

        // Act
        ResponseEntity<List<CompromissoDTO>> response = compromissoController.listarCompromissos();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
    }
}

