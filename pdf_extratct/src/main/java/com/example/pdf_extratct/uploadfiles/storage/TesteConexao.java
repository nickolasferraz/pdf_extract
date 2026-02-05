package com.example.pdf_extratct.uploadfiles.storage;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "teste_conexao")
public class TesteConexao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mensagem;

    private LocalDateTime criadoEm;

    // Construtores
    public TesteConexao() {}

    public TesteConexao(String mensagem) {
        this.mensagem = mensagem;
        this.criadoEm = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
