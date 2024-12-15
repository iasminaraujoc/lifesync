package br.com.lifesync.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import br.com.lifesync.domain.usuario.CadastroUsuarioDTO;
import br.com.lifesync.domain.usuario.LoginDTO;
import br.com.lifesync.domain.usuario.Role;
import br.com.lifesync.domain.usuario.Usuario;
import br.com.lifesync.domain.usuario.UsuarioService;
import br.com.lifesync.infra.security.TokenService;

class LoginControllerTest {

    @InjectMocks
    private LoginController loginController;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private UsuarioService usuarioService;

    public LoginControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void efetuarLogin_DeveEfetuarLoginComSucesso() {
        // Arrange
        LoginDTO loginDTO = new LoginDTO("user@example.com", "password123");
        Authentication authenticationMock = mock(Authentication.class);

        Usuario usuarioMock = new Usuario("User", "user@example.com", "encodedPassword", Role.USUARIO);
        when(authenticationMock.getPrincipal()).thenReturn(usuarioMock);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticationMock);
        when(tokenService.gerarToken(usuarioMock)).thenReturn("tokenJWT");

        // Act
        ResponseEntity<String> response = loginController.efetuarLogin(loginDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("tokenJWT", response.getBody());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenService).gerarToken(usuarioMock);
    }

    @Test
    void efetuarLogin_NaoDeveGerarTokenParaCredenciaisInvalidas() {
        // Arrange
        LoginDTO loginDTO = new LoginDTO("invalid@example.com", "wrongpassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        // Act
        try {
            loginController.efetuarLogin(loginDTO);
        } catch (Exception e) {
            // Assert
            assertEquals("Authentication failed", e.getMessage());
        }
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenService, never()).gerarToken(any(Usuario.class));
    }

    @Test
    void register_DeveCadastrarUsuarioComSucesso() {
        // Arrange
        CadastroUsuarioDTO cadastroUsuarioDTO = new CadastroUsuarioDTO("User", "user@example.com", "password123");
        when(usuarioService.loadUserByUsername("user@example.com")).thenReturn(null);

        // Act
        ResponseEntity<Void> response = loginController.register(cadastroUsuarioDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(usuarioService).salvarUsuario(any(Usuario.class));
    }

    @Test
    void register_NaoDeveCadastrarUsuarioJaExistente() {
        // Arrange
        CadastroUsuarioDTO cadastroUsuarioDTO = new CadastroUsuarioDTO("User", "user@example.com", "password123");
        Usuario usuarioExistente = new Usuario("User", "user@example.com", "encodedPassword", Role.USUARIO);
        when(usuarioService.loadUserByUsername("user@example.com")).thenReturn(usuarioExistente);

        // Act
        ResponseEntity<Void> response = loginController.register(cadastroUsuarioDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(usuarioService, never()).salvarUsuario(any());
    }

    @Test
    void register_NaoDeveCadastrarUsuarioComDadosInvalidos() {
        // Arrange
        CadastroUsuarioDTO cadastroUsuarioDTO = new CadastroUsuarioDTO("", "invalidemail", "short");

        // Act
        try {
            loginController.register(cadastroUsuarioDTO);
        } catch (Exception e) {
            // Assert
            assertEquals("javax.validation.ConstraintViolationException", e.getClass().getName());
        }
    }

}

