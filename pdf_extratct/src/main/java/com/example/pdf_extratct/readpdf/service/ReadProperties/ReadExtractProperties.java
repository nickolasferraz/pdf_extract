package com.example.pdf_extratct.readpdf.service.ReadProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class ReadExtractProperties {
    private String location="upload-dir";

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location=location;
    }

}
