package com.example.pdf_extratct.uploadfiles.storage.service;

import com.example.pdf_extratct.uploadfiles.storage.exceptions.StorageException;
import com.example.pdf_extratct.uploadfiles.storage.exceptions.StorageFileNotFoundException;
import com.example.pdf_extratct.uploadfiles.storage.properties.StorageProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List; // Import List
import java.util.stream.Stream;
@Service
public class FileSystemStorageService  implements StorageService {

    private final Path rootlocation;

    public FileSystemStorageService(StorageProperties properties) {
        if (properties.getLocation().trim().length() == 0) {
            throw new SecurityException("File upload location cannot be empty");
        }

        this.rootlocation = Paths.get(properties.getLocation()).toAbsolutePath().normalize();
        init(); // Call init here to ensure directory is created on service instantiation
    }


    @Override
    public void store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file");

            }

            Path destinationFile = this.rootlocation.resolve(
                            Paths.get(file.getOriginalFilename()))
                    .normalize().toAbsolutePath();


            if (!destinationFile.getParent().equals(this.rootlocation.toAbsolutePath())) {
                throw new StorageException("Cannot store file outside current directory.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException e) {
            throw new StorageException("Failed to store file", e);
        }

    }

    @Override
    public void storeAll(List<MultipartFile> files) {
        for (MultipartFile file : files) {
            store(file); // Reuse the existing store method for each file
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootlocation, 1)
                    .filter(path -> !path.equals(this.rootlocation))
                    .filter(Files::isRegularFile)
                    .map(this.rootlocation::relativize);
        } catch (IOException e) {
            throw new StorageException("Failed to read Stored Files");
        }
    }

    @Override
    public Path load(String fileName) {
        return rootlocation.resolve(fileName);
    }

    @Override
    public Resource loadAsResource(String fileName) {
        try {
            Path file = load(fileName);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + fileName
                );
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + fileName, e);
        }
    }

    @Override
    public void deleteAll() {
        try (Stream<Path> paths = Files.walk(rootlocation)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".pdf"))
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("Erro ao limpar uploads", e);
        }
    }


    @Override
    public void init() {

        try {
            Files.createDirectories(rootlocation);
        } catch (IOException e) {
            throw new SecurityException("Could not initialize storage");
        }

    }
}
