package br.com.lifesync.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import br.com.lifesync.domain.tarefa.CadastroTarefaDTO;
import br.com.lifesync.domain.tarefa.EdicaoTarefaDTO;
import br.com.lifesync.domain.tarefa.Tarefa;
import br.com.lifesync.domain.tarefa.TarefaService;

import java.util.Optional;

class TarefaControllerTest {

    @InjectMocks
    private TarefaController tarefaController;

    @Mock
    private TarefaService tarefaService;

    public TarefaControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void adicionarTarefa_deveAdicionarTarefaComSucesso() {
        // Arrange
        CadastroTarefaDTO cadastroTarefaDTO = new CadastroTarefaDTO("titulo", "descricao", "prazo");
        doNothing().when(tarefaService).adicionarTarefa(any(CadastroTarefaDTO.class));

        // Act
        ResponseEntity<Void> response = tarefaController.adicionarTarefa(cadastroTarefaDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(tarefaService).adicionarTarefa(any(CadastroTarefaDTO.class));
    }

    @Test
    void dobterTarefa_deveObterTarefaComSucesso() {
        // Arrange
        Long tarefaId = 1L;
        Tarefa tarefaMock = new Tarefa();
        when(tarefaService.obterTarefa(tarefaId)).thenReturn(Optional.of(tarefaMock));

        // Act
        ResponseEntity<Tarefa> response = tarefaController.obterTarefa(tarefaId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tarefaMock, response.getBody());
        verify(tarefaService).obterTarefa(tarefaId);
    }

    @Test
    void obterTarefa_deveRetornarNotFoundQuandoTarefaNaoExiste() {
        // Arrange
        Long tarefaId = 1L;
        when(tarefaService.obterTarefa(tarefaId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Tarefa> response = tarefaController.obterTarefa(tarefaId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(tarefaService).obterTarefa(tarefaId);
    }

    @Test
    void editarTarefa_deveEditarTarefaComSucesso() {
        // Arrange
        Long tarefaId = 1L;
        EdicaoTarefaDTO edicaoTarefaDTO = new EdicaoTarefaDTO("titulo", "descricao", "prazo");
        doNothing().when(tarefaService).editarTarefa(tarefaId, edicaoTarefaDTO);

        // Act
        ResponseEntity<Void> response = tarefaController.editarTarefa(tarefaId, edicaoTarefaDTO);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(tarefaService).editarTarefa(tarefaId, edicaoTarefaDTO);
    }

    @Test
    void excluirTarefa_deveExcluirTarefaComSucesso() {
        // Arrange
        Long tarefaId = 1L;
        doNothing().when(tarefaService).excluirTarefa(tarefaId);

        // Act
        ResponseEntity<Void> response = tarefaController.excluirTarefa(tarefaId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(tarefaService).excluirTarefa(tarefaId);
    }

    @Test
    void concluirTarefa_deveMarcarTarefaComoConcluidaComSucesso() {
        // Arrange
        Long tarefaId = 1L;
        doNothing().when(tarefaService).marcarTarefaComoConcluida(tarefaId);

        // Act
        ResponseEntity<Void> response = tarefaController.marcarTarefaComoConcluida(tarefaId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(tarefaService).marcarTarefaComoConcluida(tarefaId);
    }
}