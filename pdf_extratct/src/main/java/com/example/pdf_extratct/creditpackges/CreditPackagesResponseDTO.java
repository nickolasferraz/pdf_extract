package com.example.pdf_extratct.creditpackges;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // Ignora campos não mapeados como "moeda"
public class CreditPackagesResponseDTO {

    @JsonProperty("packageId") // Mapeia o campo 'packageId' do JSON para 'id'
    private int id;

    private String name;
    private int credits;

    @JsonProperty("price_cents") // Mapeia o campo 'price_cents' do JSON para 'price'
    private BigDecimal price;

    private String description;
}
