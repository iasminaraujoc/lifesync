package br.com.lifesync.domain.compromisso;

import br.com.lifesync.domain.evento.Evento;
import br.com.lifesync.domain.evento.EventoRepository;
import br.com.lifesync.domain.tarefa.Tarefa;
import br.com.lifesync.domain.tarefa.TarefaRepository;
import br.com.lifesync.domain.usuario.Usuario;
import br.com.lifesync.domain.usuario.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompromissoServiceTest {

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private CompromissoService compromissoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listarCompromissosOrdenados_DeveRetornarListaOrdenada() {
        // Arrange
        Usuario usuarioMock = mock(Usuario.class);
        when(usuarioService.obterUsuarioLogado()).thenReturn(usuarioMock);

        Tarefa tarefa = new Tarefa();
        tarefa.setId(1L);
        tarefa.setTitulo("Tarefa 1");
        tarefa.setData(LocalDate.of(2024, 12, 15));
        tarefa.setHora(LocalTime.of(10, 0));
        
        Evento evento = new Evento();
        evento.setId(2L);
        evento.setTitulo("Evento 1");
        evento.setData(LocalDate.of(2024, 12, 15));
        evento.setHora(LocalTime.of(9, 0));

        when(tarefaRepository.findByUsuarioAndAtivaTrue(usuarioMock)).thenReturn(List.of(tarefa));
        when(eventoRepository.findByUsuarioAndAtivoTrue(usuarioMock)).thenReturn(List.of(evento));

        // Act
        List<CompromissoDTO> result = compromissoService.listarCompromissosOrdenados();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Evento 1", result.get(0).titulo()); // Primeiro compromisso deve ser o evento
        assertEquals("Tarefa 1", result.get(1).titulo()); // Segundo compromisso deve ser a tarefa
        verify(tarefaRepository, times(1)).findByUsuarioAndAtivaTrue(usuarioMock);
        verify(eventoRepository, times(1)).findByUsuarioAndAtivoTrue(usuarioMock);
    }

    @Test
    void listarCompromissosOrdenados_ListaVazia() {
        // Arrange
        Usuario usuarioMock = mock(Usuario.class);
        when(usuarioService.obterUsuarioLogado()).thenReturn(usuarioMock);
        when(tarefaRepository.findByUsuarioAndAtivaTrue(usuarioMock)).thenReturn(List.of());
        when(eventoRepository.findByUsuarioAndAtivoTrue(usuarioMock)).thenReturn(List.of());

        // Act
        List<CompromissoDTO> result = compromissoService.listarCompromissosOrdenados();

        // Assert
        assertTrue(result.isEmpty());
        verify(tarefaRepository, times(1)).findByUsuarioAndAtivaTrue(usuarioMock);
        verify(eventoRepository, times(1)).findByUsuarioAndAtivoTrue(usuarioMock);
    }

    @Test
    void listarCompromissosOrdenados_DeveManterOrdemCorretaComDatasIguais() {
        // Arrange
        Usuario usuarioMock = mock(Usuario.class);
        when(usuarioService.obterUsuarioLogado()).thenReturn(usuarioMock);

        Tarefa tarefa = new Tarefa();
        tarefa.setId(1L);
        tarefa.setTitulo("Tarefa 1");
        tarefa.setData(LocalDate.of(2024, 12, 15));
        tarefa.setHora(LocalTime.of(10, 0));
        
        Evento evento = new Evento();
        evento.setId(2L);
        evento.setTitulo("Evento 1");
        evento.setData(LocalDate.of(2024, 12, 15));
        evento.setHora(LocalTime.of(8, 0));

        when(tarefaRepository.findByUsuarioAndAtivaTrue(usuarioMock)).thenReturn(List.of(tarefa));
        when(eventoRepository.findByUsuarioAndAtivoTrue(usuarioMock)).thenReturn(List.of(evento));

        // Act
        List<CompromissoDTO> result = compromissoService.listarCompromissosOrdenados();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Evento 1", result.get(0).titulo());
        assertEquals("Tarefa 1", result.get(1).titulo());
    }

    @Test
    void listarCompromissosOrdenados_UsuarioNaoLogado() {
        // Arrange
        when(usuarioService.obterUsuarioLogado()).thenThrow(new RuntimeException("Usuário não autenticado"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> compromissoService.listarCompromissosOrdenados());
        verifyNoInteractions(tarefaRepository, eventoRepository);
    }
}
