package com.example.pdf_extratct;

import com.example.pdf_extratct.readpdf.service.ReadProperties.ReadExtractProperties;
import com.example.pdf_extratct.uploadfiles.storage.properties.StorageProperties;
import com.example.pdf_extratct.uploadfiles.storage.service.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
@EnableConfigurationProperties({StorageProperties.class,ReadExtractProperties.class})
public class PdfExtratctApplication {

	public static void main(String[] args) {

        SpringApplication.run(PdfExtratctApplication.class, args);
	}

    @Bean
    CommandLineRunner init(StorageService storageService) {
        return (args) -> {
            storageService.deleteAll();
            storageService.init();
        };
    }

}
