package com.optimagrowth.license.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.springframework.hateoas.RepresentationModel;

@Entity
public class License extends RepresentationModel<License> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long licenseId;
    private String description;
    private Long organizationId;
    private String productName;
    private String licenseType;
    private String comment;

    public Long getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(Long licenseId) {
        this.licenseId = licenseId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public License withComment(String comment) {
        this.setComment(comment);
        return this;
    }

    @Override
    public String toString() {
        return "License{" +
                "licenseId=" + licenseId +
                ", description='" + description + '\'' +
                ", organizationId=" + organizationId +
                ", productName='" + productName + '\'' +
                ", licenseType='" + licenseType + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}
