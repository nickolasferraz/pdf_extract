package com.example.pdf_extratct.loginpage.auth.service;


import com.example.pdf_extratct.loginpage.auth.AuthAccountEntity;
import com.example.pdf_extratct.loginpage.auth.AuthAccountRepository;
import com.example.pdf_extratct.loginpage.auth.AuthProvider;
import com.example.pdf_extratct.loginpage.user.LoginDtos.LoginRequestDto;
import com.example.pdf_extratct.loginpage.user.LoginDtos.LoginResponseDto;
import com.example.pdf_extratct.loginpage.user.RegisterDtoRequest.UserRegisterRequestDto;
import com.example.pdf_extratct.loginpage.user.strategy.UserCreationStrategyFactory;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import com.example.pdf_extratct.loginpage.user.UserRepository;
import com.example.pdf_extratct.security.JwtUtil;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthAccountRepository authAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserCreationStrategyFactory strategyFactory;

    public AuthService(
            UserRepository userRepository,
            AuthAccountRepository authAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            UserCreationStrategyFactory strategyFactory

    )
    {
        this.userRepository = userRepository;
        this.authAccountRepository = authAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.strategyFactory = strategyFactory;
            }


    @Transactional
    public LoginResponseDto register(UserRegisterRequestDto request){

        if (userRepository.existsByEmail(request.email())){
            throw new RuntimeException("Email já cadastrado");
        }

        // Obter strategy
        var strategy = strategyFactory.getStrategy(AuthProvider.LOCAL);

        // Criar e salvar usuário
        var user = strategy.createUser(request);
        user = userRepository.save(user);

        // Criar e salvar conta de autenticação
        var authAccount = strategy.createAuthAccount(user, request, passwordEncoder);
        authAccountRepository.save(authAccount);

        // Gerar token JWT
        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail());

        return new LoginResponseDto(
                token,
                user.getUserId(),
                user.getEmail(),
                user.getCreditBalance()
        );
    }

    @Transactional
    public LoginResponseDto login(LoginRequestDto request){
        UserEntity userEntity = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        AuthAccountEntity authAccount = authAccountRepository
                .findByUserAndProvider(userEntity, AuthProvider.LOCAL)
                .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));

        if (!passwordEncoder.matches(request.password(), authAccount.getPasswordHash())) {
            throw new RuntimeException("Senha incorreta");
        }
        String token = jwtUtil.generateToken(userEntity.getUserId(), userEntity.getEmail());

        return new LoginResponseDto(
                token,
                userEntity.getUserId(),
                userEntity.getEmail(),
                userEntity.getCreditBalance()
        );




    }


}
