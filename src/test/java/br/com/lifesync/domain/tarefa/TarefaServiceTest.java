package br.com.lifesync.domain.tarefa;

import br.com.lifesync.domain.usuario.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class TarefaServiceTest {

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private TarefaService tarefaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void editarTarefa_deveAtualizarTarefaExistente() {
        EdicaoTarefaDTO dto = new EdicaoTarefaDTO("Tarefa Editada", "2024-12-16", "12:00");
        
        Tarefa tarefaExistente = new Tarefa(new CadastroTarefaDTO("Tarefa Teste", "2024-12-15", "10:00"));
        tarefaExistente.setId(1L);
        
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefaExistente));
        
        tarefaService.editarTarefa(1L, dto);
        
        assertEquals("Tarefa Editada", tarefaExistente.getTitulo());
        assertEquals("2024-12-16", tarefaExistente.getData().toString());
        assertEquals("12:00", tarefaExistente.getHora().toString());
    }

    @Test
    void excluirTarefa_deveDesativarTarefaExistente() {
        Tarefa tarefaExistente = new Tarefa(new CadastroTarefaDTO("Tarefa Teste", "2024-12-15", "10:00"));
        tarefaExistente.setId(1L);

        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefaExistente));

        tarefaService.excluirTarefa(1L);

        assertFalse(tarefaExistente.isAtiva());
    }

    @Test
    void marcarTarefaComoConcluida_deveConcluirTarefaExistente() {
        Tarefa tarefaExistente = new Tarefa(new CadastroTarefaDTO("Tarefa Teste", "2024-12-15", "10:00"));
        tarefaExistente.setId(1L);

        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefaExistente));

        tarefaService.marcarTarefaComoConcluida(1L);

        assertTrue(tarefaExistente.isConcluida());
    }

    @Test
    void obterTarefa_deveRetornarTarefaExistente() {
        Tarefa tarefaExistente = new Tarefa(new CadastroTarefaDTO("Tarefa Teste", "2024-12-15", "10:00"));
        tarefaExistente.setId(1L);

        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefaExistente));

        Optional<Tarefa> tarefa = tarefaService.obterTarefa(1L);

        assertTrue(tarefa.isPresent());
        assertEquals(1L, tarefa.get().getId());
    }
}
