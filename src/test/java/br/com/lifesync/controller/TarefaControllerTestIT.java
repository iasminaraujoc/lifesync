package br.com.lifesync.controller;

import br.com.lifesync.domain.tarefa.Tarefa;
import br.com.lifesync.domain.tarefa.TarefaRepository;
import br.com.lifesync.domain.usuario.Usuario;
import br.com.lifesync.domain.usuario.UsuarioRepository;
import br.com.lifesync.domain.tarefa.CadastroTarefaDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@Transactional
public class TarefaControllerTestIT {

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
    private TarefaController tarefaController;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tarefaController).build();
    }

    @Test
    @DisplayName("Deve criar uma tarefa e marcá-la como concluída")
    @WithMockUser(username = "usuario1@email.com", roles = {"USER"})
    public void deveCriarUmaTarefaEMarcarComoConcluida() throws Exception {
        // Simula o usuário autenticado
        Usuario usuario = new Usuario();
        usuario.setEmail("usuario1@email.com");
        usuario = usuarioRepository.save(usuario);

        // DTO para criar uma nova tarefa (usando Strings no formato correto)
        CadastroTarefaDTO cadastroTarefaDTO = new CadastroTarefaDTO(
                "Tarefa Teste",
                "2025-01-12", // Formato yyyy-MM-dd
                "14:00"       // Formato HH:mm
        );

        // Envia requisição para criar a tarefa
        mockMvc.perform(post("/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cadastroTarefaDTO)))
                .andExpect(status().isCreated());

        // Recupera a tarefa criada
        Tarefa tarefa = tarefaRepository.findAll().get(0);

        // Marca a tarefa como concluída
        mockMvc.perform(patch("/tarefas/" + tarefa.getId() + "/concluir")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verifica que a tarefa foi concluída no banco
        tarefa = tarefaRepository.findById(tarefa.getId()).orElseThrow();
        assert tarefa.isConcluida();
    }

    @Test
    @DisplayName("Deve editar uma tarefa existente")
    @WithMockUser(username = "usuario1@email.com", roles = {"USER"})
    public void deveEditarUmaTarefaExistente() throws Exception {
        // Cria e salva uma tarefa no banco
        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo("Tarefa Original");
        tarefa.setData(LocalDate.parse("2025-01-12"));
        tarefa.setHora(LocalTime.parse("14:00"));
        tarefa = tarefaRepository.save(tarefa);

        // DTO para edição da tarefa
        String tarefaEditadaJson = objectMapper.writeValueAsString(new CadastroTarefaDTO(
                "Tarefa Editada",
                "2025-01-13", // Nova data
                "15:00"       // Nova hora
        ));

        // Envia requisição para editar a tarefa
        mockMvc.perform(put("/tarefas/" + tarefa.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tarefaEditadaJson))
                .andExpect(status().isNoContent());

        // Verifica se a tarefa foi editada no banco
        Tarefa tarefaEditada = tarefaRepository.findById(tarefa.getId()).orElseThrow();
        assert tarefaEditada.getTitulo().equals("Tarefa Editada");
        assert tarefaEditada.getData().equals(LocalDate.parse("2025-01-13"));
        assert tarefaEditada.getHora().equals(LocalTime.parse("15:00"));
    }

    @Test
    @DisplayName("Deve desativar uma tarefa existente")
    @WithMockUser(username = "usuario1@email.com", roles = {"USER"})
    public void deveDesativarUmaTarefaExistente() throws Exception {
        // Cria e salva uma tarefa no banco
        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo("Tarefa para Desativar");
        tarefa.setData(LocalDate.parse("2025-01-12"));
        tarefa.setHora(LocalTime.parse("14:00"));
        tarefa = tarefaRepository.save(tarefa);
    
        // Verifica que a tarefa foi salva no banco
        assert tarefaRepository.existsById(tarefa.getId());
    
        // Envia requisição para desativar a tarefa (não excluir)
        mockMvc.perform(delete("/tarefas/" + tarefa.getId()))
                .andExpect(status().isNoContent());
    
        // Recupera a tarefa para verificar seu status
        Optional<Tarefa> tarefaDesativada = tarefaRepository.findById(tarefa.getId());
        
        // Verifica se a tarefa existe no banco, mas está desativada
        assert tarefaDesativada.isPresent();
        assert !tarefaDesativada.get().isAtiva();  // Verifica se a tarefa foi desativada
    }


}
