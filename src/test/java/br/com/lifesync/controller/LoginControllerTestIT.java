package br.com.lifesync.controller;

import br.com.lifesync.LifesyncApplication;
import br.com.lifesync.domain.usuario.CadastroUsuarioDTO;
import br.com.lifesync.domain.usuario.LoginDTO;
import br.com.lifesync.domain.usuario.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@Transactional 
class LoginControllerTestIT {

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

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LoginController loginController;

    @DynamicPropertySource
    static void configureTestcontainers(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @Test
    @DisplayName("Deve registrar um novo usuário e permitir login")
    void deveRegistrarUsuarioEPermitirLogin() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(loginController).build();

        // Dados do novo usuário
        CadastroUsuarioDTO novoUsuario = new CadastroUsuarioDTO(
                "Teste Usuario", 
                "usuario.teste@email.com", 
                "senha123");

        // Requisição para registrar o usuário
        mockMvc.perform(post("/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoUsuario)))
                .andExpect(status().isOk()); // Verifica que o registro foi bem-sucedido

        // Verifica que o usuário foi salvo no banco
        assert (usuarioRepository.findByEmail("usuario.teste@email.com")!= null);

        // Dados para login
        LoginDTO loginDTO = new LoginDTO(
                "usuario.teste@email.com", 
                "senha123");

        // Requisição para realizar o login
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk()) 
                .andExpect(content().contentType(MediaType.TEXT_PLAIN + ";charset=ISO-8859-1")) 
                .andExpect(content().string(org.hamcrest.Matchers.notNullValue())); 
    }
}

