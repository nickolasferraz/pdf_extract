package com.example.pdf_extratct.loginpage.controllers;

import com.example.pdf_extratct.loginpage.auth.service.AuthService;
import com.example.pdf_extratct.loginpage.user.LoginDtos.LoginRequestDto;
import com.example.pdf_extratct.loginpage.user.LoginDtos.LoginResponseDto;
// import com.example.pdf_extratct.loginpage.user.LoginDtos.RegisterResponseDto; // Removido import de RegisterResponseDto
import com.example.pdf_extratct.loginpage.user.RegisterDtoRequest.UserRegisterRequestDto;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import com.example.pdf_extratct.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

// Importações do Swagger/OpenAPI
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody; // Mantido para a anotação @RequestBody do Swagger
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;


@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação e Usuários", description = "APIs para registro, login, informações do usuário e gerenciamento de sessão.")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário",
            description = "Cria uma nova conta de usuário com email e senha.")
    @RequestBody(description = "Dados para registro de usuário", required = true,
            content = @Content(schema = @Schema(implementation = UserRegisterRequestDto.class)))
    @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso",
            content = @Content(schema = @Schema(implementation = LoginResponseDto.class))) // Alterado para LoginResponseDto
    @ApiResponse(responseCode = "400", description = "Dados de registro inválidos ou email já em uso",
            content = @Content(schema = @Schema(implementation = Map.class)))
    public ResponseEntity<LoginResponseDto> register( // Alterado tipo de retorno
            @Valid @org.springframework.web.bind.annotation.RequestBody UserRegisterRequestDto request) { // Usar @RequestBody completo para evitar conflito com Swagger
        // Removido try-catch, exceções serão tratadas por GlobalExceptionHandler
        // Removidos logs de depuração
        LoginResponseDto response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login de usuário",
            description = "Autentica um usuário e retorna um token JWT para acesso às APIs protegidas.")
    @RequestBody(description = "Credenciais de login (email e senha)", required = true,
            content = @Content(schema = @Schema(implementation = LoginRequestDto.class)))
    @ApiResponse(responseCode = "200", description = "Login bem-sucedido, retorna token JWT",
            content = @Content(schema = @Schema(implementation = LoginResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Credenciais inválidas",
            content = @Content(schema = @Schema(implementation = Map.class)))
    public ResponseEntity<?> login(@Valid @org.springframework.web.bind.annotation.RequestBody LoginRequestDto request,
                                   HttpServletResponse response) {
        try {
            LoginResponseDto loginResponse = authService.login(request);

            String token = loginResponse.token();

            String cookie = "JWT=" + token +
                    "; Path=/" +
                    "; HttpOnly" +
                    "; Max-Age=" + (7 * 24 * 60 * 60) +
                    "; SameSite=Lax";

            response.addHeader("Set-Cookie", cookie);

            return ResponseEntity.ok(loginResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Obter informações do usuário autenticado",
            description = "Retorna detalhes do usuário atualmente autenticado.")
    @ApiResponse(responseCode = "200", description = "Informações do usuário retornadas com sucesso",
            content = @Content(schema = @Schema(implementation = Map.class)))
    @ApiResponse(responseCode = "401", description = "Não autorizado / Token inválido", content = @Content)
    public ResponseEntity<?> inforuser(@AuthenticationPrincipal UserEntity user) {
        if (user == null) {
            return ResponseEntity.status(401).body(
                    Map.of("error", "Não autenticado")
            );
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getUserId());
        userInfo.put("email", user.getEmail());
        userInfo.put("creditBalance", user.getCreditBalance());
        userInfo.put("emailValidado", user.getEmailValidado());
        userInfo.put("createdAt", user.getCreatedAt());

        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar token JWT",
            description = "Gera um novo token JWT para o usuário autenticado, estendendo a sessão.")
    @ApiResponse(responseCode = "200", description = "Token renovado com sucesso",
            content = @Content(schema = @Schema(implementation = LoginResponseDto.class)))
    @ApiResponse(responseCode = "401", description = "Não autorizado / Token inválido", content = @Content)
    @ApiResponse(responseCode = "400", description = "Erro ao renovar token",
            content = @Content(schema = @Schema(implementation = Map.class)))
    public ResponseEntity<?> refresh(@AuthenticationPrincipal UserEntity user) {
        if (user == null) {
            return ResponseEntity.status(401).body(
                    Map.of("error", "Token inválido")
            );
        }

        try {
            String newToken = jwtUtil.generateToken(user.getUserId(), user.getEmail());

            LoginResponseDto response = new LoginResponseDto(
                    newToken,
                    user.getUserId(),
                    user.getEmail(),
                    user.getCreditBalance()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Erro ao renovar token")
            );
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout de usuário",
            description = "Limpa o cookie JWT do navegador.")
    @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso",
            content = @Content(schema = @Schema(implementation = Map.class)))
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Apaga o cookie setando Max-Age=0
        String cookieHeader = "JWT=; Path=/; HttpOnly; Max-Age=0; SameSite=Lax";
        response.addHeader("Set-Cookie", cookieHeader);
        return ResponseEntity.ok(
                Map.of("message", "Logout realizado com sucesso")
        );
    }
}
