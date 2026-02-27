package com.example.pdf_extratct.Payment.models.entity;


import com.example.pdf_extratct.Payment.Enum.NamePackageEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="credit_packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditPackagesEntity {
    @Id
    @Column(name="package_id")
    private int packageId ;

    @Enumerated(EnumType.STRING)
    @Column(name = "name")
    private NamePackageEnum name ;

    @Column(name="credits")
    private int credits;

    @Column(name = "price_cents")
    private int price_cents;

    @Column(name = "currency")
    private String moeda;


}
