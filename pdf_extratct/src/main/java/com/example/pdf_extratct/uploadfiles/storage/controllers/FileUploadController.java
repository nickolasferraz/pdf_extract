package com.example.pdf_extratct.uploadfiles.storage.controllers;

import java.util.List; // Import List
import java.util.stream.Collectors;

import com.example.pdf_extratct.loginpage.jobs.ProcessingJobService;
import com.example.pdf_extratct.uploadfiles.storage.exceptions.StorageException;
import com.example.pdf_extratct.uploadfiles.storage.exceptions.StorageFileNotFoundException;
import com.example.pdf_extratct.uploadfiles.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Slf4j
@RestController
public class FileUploadController {

    private final StorageService storageService;
    private final ProcessingJobService jobService;
    public FileUploadController(StorageService storageService
                                , ProcessingJobService jobService) {
        this.storageService = storageService;
        this.jobService = jobService;
    }

    @GetMapping("/api/files/list")
    public String listUploadedFiles(Model model) {

        model.addAttribute("files", storageService.loadAll().map(
                        path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                                "serveFile", path.getFileName().toString()).build().toUri().toString())
                .collect(Collectors.toList()));

        return "uploadForm.html";
    }

    @GetMapping("/api/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);

        if (file == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }



    @PostMapping("/api/files")

    public String handleFileUpload(@RequestParam("files") List<MultipartFile> files, // Changed to List<MultipartFile> and param name to "files"
                                   RedirectAttributes redirectAttributes) {

        try {
            storageService.storeAll(files); // Call storeAll for batch upload
            redirectAttributes.addFlashAttribute("message",
                    "You successfully uploaded " + files.size() + " files!");
            log.info("Files uploaded successfully!");

            return "redirect:/api/files/list";

        } catch (RuntimeException e) {
            log.error("Erro ao fazer o upload dos arquivos", e);
            throw new RuntimeException( "Erro ao fazer o upload dos arquivos",e);
        }
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
