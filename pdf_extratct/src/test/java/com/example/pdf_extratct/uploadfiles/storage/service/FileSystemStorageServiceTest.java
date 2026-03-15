package com.example.pdf_extratct.uploadfiles.storage.service;

import com.example.pdf_extratct.uploadfiles.storage.exceptions.StorageException;
import com.example.pdf_extratct.uploadfiles.storage.properties.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemStorageServiceTest {

    private FileSystemStorageService storageService;
    private Path rootLocation;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        rootLocation = tempDir;
        StorageProperties properties = new StorageProperties();
        properties.setLocation(tempDir.toString());
        storageService = new FileSystemStorageService(properties);
    }

    @Test
    @DisplayName("Deve armazenar um arquivo válido")
    void shouldStoreValidFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "conteudo".getBytes()
        );

        storageService.store(file);

        assertTrue(Files.exists(rootLocation.resolve("test.pdf")));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar carregar um arquivo inexistente")
    void shouldThrowExceptionWhenFileDoesNotExist() {
        assertThrows(RuntimeException.class, () -> storageService.loadAsResource("non-existent.pdf"));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar armazenar arquivo vazio")
    void shouldThrowExceptionForEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]
        );

        StorageException ex = assertThrows(StorageException.class, () -> storageService.store(file));
        assertEquals("Failed to store empty file", ex.getMessage());
    }

    @Test
    @DisplayName("Deve bloquear Path Traversal (tentativa de salvar fora do diretório raiz)")
    void shouldBlockPathTraversal() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "../malicious.pdf", "application/pdf", "conteudo".getBytes()
        );

        StorageException ex = assertThrows(StorageException.class, () -> storageService.store(file));
        assertTrue(ex.getMessage().contains("Cannot store file outside current directory"));
    }

    @Test
    @DisplayName("deleteAll deve remover apenas arquivos PDF")
    void shouldDeleteOnlyPdfFiles() throws Exception {
        Files.createFile(rootLocation.resolve("to-keep.txt"));
        Files.createFile(rootLocation.resolve("to-delete.pdf"));

        storageService.deleteAll();

        assertFalse(Files.exists(rootLocation.resolve("to-delete.pdf")));
        assertTrue(Files.exists(rootLocation.resolve("to-keep.txt")));
    }
}
