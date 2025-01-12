package br.com.lifesync.controller;

import br.com.lifesync.domain.evento.CadastroEventoDTO;
import br.com.lifesync.domain.evento.EdicaoEventoDTO;
import br.com.lifesync.domain.evento.Evento;
import br.com.lifesync.domain.evento.EventoRepository;
import br.com.lifesync.domain.evento.EventoService;
import br.com.lifesync.domain.usuario.Usuario;
import br.com.lifesync.domain.usuario.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@Transactional
public class EventoControllerTestIT {

    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("lifesync-teste")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureTestcontainers(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private EventoController eventoController;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventoController).build();
    }

    @Test
    @DisplayName("Deve criar um evento")
    @WithMockUser(username = "usuario1@email.com", roles = {"USER"})
    public void deveCriarUmEvento() throws Exception {
        // Simula o usuário autenticado
        Usuario usuario = new Usuario();
        usuario.setEmail("usuario1@email.com");
        usuario = usuarioRepository.save(usuario);

        // DTO para criar um novo evento (usando Strings no formato correto)
        CadastroEventoDTO cadastroEventoDTO = new CadastroEventoDTO(
                "Evento Teste",
                "2025-01-12", // Formato yyyy-MM-dd
                "14:00",      // Formato HH:mm
                "Local Teste"
        );

        // Envia requisição para criar o evento
        mockMvc.perform(post("/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cadastroEventoDTO)))
                .andExpect(status().isCreated());

        // Verifica se o evento foi salvo no banco
        Evento evento = eventoRepository.findAll().get(0);
        assert evento != null;
        assert evento.getTitulo().equals("Evento Teste");
        assert evento.getData().equals(LocalDate.parse("2025-01-12"));
        assert evento.getHora().equals(LocalTime.parse("14:00"));
        assert evento.getLocal().equals("Local Teste");
    }

    @Test
    @DisplayName("Deve editar um evento existente")
    @WithMockUser(username = "usuario1@email.com", roles = {"USER"})
    public void deveEditarUmEventoExistente() throws Exception {
        // Cria e salva um evento no banco
        Evento evento = new Evento();
        evento.setTitulo("Evento Original");
        evento.setData(LocalDate.parse("2025-01-12"));
        evento.setHora(LocalTime.parse("14:00"));
        evento.setLocal("Local Original");
        evento = eventoRepository.save(evento);

        // DTO para edição do evento
        EdicaoEventoDTO edicaoEventoDTO = new EdicaoEventoDTO(
                "Evento Editado",
                "2025-01-13", // Nova data
                "15:00",      // Nova hora
                "Novo Local"
        );

        // Envia requisição para editar o evento
        mockMvc.perform(put("/eventos/" + evento.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(edicaoEventoDTO)))
                .andExpect(status().isNoContent());

        // Verifica se o evento foi editado no banco
        Evento eventoEditado = eventoRepository.findById(evento.getId()).orElseThrow();
        assert eventoEditado.getTitulo().equals("Evento Editado");
        assert eventoEditado.getData().equals(LocalDate.parse("2025-01-13"));
        assert eventoEditado.getHora().equals(LocalTime.parse("15:00"));
        assert eventoEditado.getLocal().equals("Novo Local");
    }

    @Test
    @DisplayName("Deve desativar um evento existente")
    @WithMockUser(username = "usuario1@email.com", roles = {"USER"})
    public void deveDesativarUmEventoExistente() throws Exception {
        // Cria e salva um evento no banco
        Evento evento = new Evento();
        evento.setTitulo("Evento para Desativar");
        evento.setData(LocalDate.parse("2025-01-12"));
        evento.setHora(LocalTime.parse("14:00"));
        evento.setLocal("Local para Desativar");
        evento = eventoRepository.save(evento);

        // Verifica que o evento foi salvo no banco
        assert eventoRepository.existsById(evento.getId());

        // Envia requisição para desativar o evento (não excluir)
        mockMvc.perform(delete("/eventos/" + evento.getId()))
                .andExpect(status().isNoContent());

        // Recupera o evento para verificar seu status
        Optional<Evento> eventoDesativado = eventoRepository.findById(evento.getId());

        // Verifica se o evento existe no banco, mas está desativado
        assert eventoDesativado.isPresent();
        assert !eventoDesativado.get().isAtivo();  // Verifica se o evento foi desativado
    }
}
