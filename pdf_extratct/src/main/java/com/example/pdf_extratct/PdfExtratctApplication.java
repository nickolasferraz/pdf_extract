package com.example.pdf_extratct;

import com.example.pdf_extratct.readpdf.service.ReadProperties.ReadExtractProperties;
import com.example.pdf_extratct.uploadfiles.storage.properties.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({StorageProperties.class, ReadExtractProperties.class})
public class PdfExtratctApplication {

    public static void main(String[] args) {
        SpringApplication.run(PdfExtratctApplication.class, args);
    }

}
