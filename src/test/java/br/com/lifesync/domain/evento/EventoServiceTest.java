package br.com.lifesync.domain.compromisso;

import br.com.lifesync.domain.evento.Evento;
import br.com.lifesync.domain.evento.EventoRepository;
import br.com.lifesync.domain.evento.EventoService;
import br.com.lifesync.domain.evento.CadastroEventoDTO;
import br.com.lifesync.domain.evento.EdicaoEventoDTO;
import br.com.lifesync.domain.usuario.Usuario;
import br.com.lifesync.domain.usuario.UsuarioService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventoServiceTest {

    @Mock
    private EventoRepository eventoRepository;
    
    @Mock
    private UsuarioService usuarioService;
    
    @InjectMocks
    private EventoService eventoService;
    
    private CadastroEventoDTO cadastroEventoDTO;
    private EdicaoEventoDTO edicaoEventoDTO;
    private Usuario usuarioLogado;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cadastroEventoDTO = new CadastroEventoDTO(
            "Evento Teste", 
            "2024-12-15", 
            "10:00", 
            "Local Teste"
        );
        edicaoEventoDTO = new EdicaoEventoDTO(
            "Evento Editado", 
            "2024-12-16", 
            "11:00", 
            "Local Editado"
        );
    }

    @Test
    void editarEvento_deveAtualizarEventoExistente() {
        Evento eventoExistente = new Evento(cadastroEventoDTO);
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(eventoExistente));
        eventoService.editarEvento(1L, edicaoEventoDTO);
        
        assertEquals("Evento Editado", eventoExistente.getTitulo());
        assertEquals("2024-12-16", eventoExistente.getData().toString());
        assertEquals("11:00", eventoExistente.getHora().toString());
        assertEquals("Local Editado", eventoExistente.getLocal());
        
        verify(eventoRepository, times(1)).save(eventoExistente);
    }


    @Test
    void excluirEvento_deveDesativarEventoExistente() {
        Evento eventoExistente = new Evento(cadastroEventoDTO);
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(eventoExistente));
        eventoService.excluirEvento(1L);
        assertFalse(eventoExistente.isAtivo());
    }


    @Test
    void obterEvento_deveRetornarEventoExistente() {
        Evento eventoExistente = new Evento(cadastroEventoDTO);
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(eventoExistente));
        Optional<Evento> evento = eventoService.obterEvento(1L);
        assertTrue(evento.isPresent());
        assertEquals(eventoExistente, evento.get());
    }
}