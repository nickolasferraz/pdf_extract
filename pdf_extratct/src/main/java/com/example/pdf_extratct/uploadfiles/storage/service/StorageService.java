package com.example.pdf_extratct.uploadfiles.storage.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List; // Import List
import java.util.stream.Stream;

public interface StorageService {

    void init();

    void store(MultipartFile file);

    void storeAll(List<MultipartFile> files); // New method for batch uploads

    Stream<Path> loadAll();
    Path load(String fileName);

    Resource loadAsResource(String fileName);

    void deleteAll();
}
