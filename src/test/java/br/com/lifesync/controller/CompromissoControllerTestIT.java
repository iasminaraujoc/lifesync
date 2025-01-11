package br.com.lifesync.controller;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.springframework.http.MediaType;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import br.com.lifesync.domain.evento.Evento;
import br.com.lifesync.domain.evento.EventoRepository;
import br.com.lifesync.domain.tarefa.Tarefa;
import br.com.lifesync.domain.tarefa.TarefaRepository;
import br.com.lifesync.domain.usuario.Usuario;
import br.com.lifesync.domain.usuario.UsuarioRepository;
import jakarta.transaction.Transactional;

@SpringBootTest
@Testcontainers
@Transactional
public class CompromissoControllerTestIT { 

    // configurações associadas a integracção com banco de dados
    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("lifesync-teste")
            .withUsername("test")
            .withPassword("test");

    @AfterAll
    static void tearDown() {
        if (mysqlContainer != null) {
            mysqlContainer.stop();
        }
    }

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TarefaRepository tarefaRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private CompromissoController compromissoController;

    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configureTestcontainers(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(compromissoController).build();
    }

    @Test
    @DisplayName("Deve listar apenas compromissos do usuário autenticado")
    @WithMockUser(username = "usuario1@email.com")// Simula um usuário autenticado
    public void deveListarApenasCompromissosDoUsuarioAutenticado() throws Exception {
        // Configura o usuário autenticado no banco
        Usuario usuarioAutenticado = new Usuario();
        usuarioAutenticado.setEmail("usuario1@email.com");
        usuarioAutenticado = usuarioRepository.save(usuarioAutenticado);

        // Configura outro usuário no banco
        Usuario outroUsuario = new Usuario();
        outroUsuario.setEmail("usuario2@email.com");
        outroUsuario = usuarioRepository.save(outroUsuario);

        // Insere compromissos do usuário autenticado (usuário 1)
        Tarefa tarefaDoUsuarioAutenticado = new Tarefa();
        tarefaDoUsuarioAutenticado.setTitulo("Tarefa 1");
        tarefaDoUsuarioAutenticado.setData(java.time.LocalDate.of(2025, 1, 10));
        tarefaDoUsuarioAutenticado.setHora(java.time.LocalTime.of(14, 0));
        tarefaDoUsuarioAutenticado.setUsuario(usuarioAutenticado);
        tarefaDoUsuarioAutenticado.ativar();
        tarefaRepository.save(tarefaDoUsuarioAutenticado);

        Evento eventoDoUsuarioAutenticado = new Evento();
        eventoDoUsuarioAutenticado.setTitulo("Evento 1");
        eventoDoUsuarioAutenticado.setData(java.time.LocalDate.of(2025, 1, 11));
        eventoDoUsuarioAutenticado.setHora(java.time.LocalTime.of(16, 0));
        eventoDoUsuarioAutenticado.setUsuario(usuarioAutenticado);
        eventoDoUsuarioAutenticado.ativar();
        eventoRepository.save(eventoDoUsuarioAutenticado);

        // Insere um compromisso que não pertence ao usuário autenticado (usuário 2)
        Tarefa tarefaDeOutroUsuario = new Tarefa();
        tarefaDeOutroUsuario.setTitulo("Tarefa 2");
        tarefaDeOutroUsuario.setData(java.time.LocalDate.of(2025, 1, 12));
        tarefaDeOutroUsuario.setHora(java.time.LocalTime.of(18, 0));
        tarefaDeOutroUsuario.setUsuario(outroUsuario);
        tarefaDeOutroUsuario.ativar();
        tarefaRepository.save(tarefaDeOutroUsuario);

        // Realiza a requisição ao endpoint protegido
        mockMvc.perform(get("/compromissos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) 
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) 
                .andExpect(jsonPath("$", hasSize(2))) 
                .andExpect(jsonPath("$[0].titulo", is("Tarefa 1"))) 
                .andExpect(jsonPath("$[0].tipo", is("tarefa"))) 
                .andExpect(jsonPath("$[1].titulo", is("Evento 1"))) 
                .andExpect(jsonPath("$[1].tipo", is("evento"))); 

    }
}
