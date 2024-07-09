package fr.uttop.normalization.services;

import com.github.owlcs.ontapi.Ontology;
import fr.uttop.normalization.entities.NormalizedOntology;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public interface NormalizationService {

    public File convertMultiPartToFile(MultipartFile file) throws IOException;

    public NormalizedOntology firstNormalization(NormalizedOntology ontology);

}

