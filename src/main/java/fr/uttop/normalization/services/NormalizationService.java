package fr.uttop.normalization.services;

import com.github.owlcs.ontapi.Ontology;
import fr.uttop.normalization.dto.NormalizationDTO;
import fr.uttop.normalization.entities.NormalizedOntology;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public interface NormalizationService {

    public NormalizedOntology firstNormalization(NormalizedOntology ontology);

    public NormalizedOntology secondNormalization(NormalizedOntology ontology);

    public NormalizedOntology thirdNormalization(NormalizedOntology ontology);

    public NormalizedOntology normalizeOntology(NormalizedOntology ontology);

}

