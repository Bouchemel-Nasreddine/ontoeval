package fr.uttop.normalization.dto;

import org.springframework.web.multipart.MultipartFile;

public class NormalizationDTO {


    private MultipartFile ontologyFile;
    private String ontologyUrl;

    public NormalizationDTO() {
    }

    public NormalizationDTO(MultipartFile ontologyFile) {
        this.ontologyFile = ontologyFile;
    }

    public NormalizationDTO(String ontologyUrl) {
        this.ontologyUrl = ontologyUrl;
    }

    public MultipartFile getOntologyFile() {
        return ontologyFile;
    }

    public void setOntologyFile(MultipartFile ontologyFile) {
        this.ontologyFile = ontologyFile;
    }

    public String getOntologyUrl() {
        return ontologyUrl;
    }

    public void setOntologyUrl(String ontologyUrl) {
        this.ontologyUrl = ontologyUrl;
    }
}
