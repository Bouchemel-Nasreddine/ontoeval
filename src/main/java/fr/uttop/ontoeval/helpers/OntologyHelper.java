package fr.uttop.ontoeval.helpers;

import com.github.owlcs.ontapi.DataFactory;
import com.github.owlcs.ontapi.OntManagers;
import com.github.owlcs.ontapi.Ontology;
import com.github.owlcs.ontapi.OntologyManager;
import fr.uttop.ontoeval.config.AppConfig;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.io.OWLRendererIOException;
import org.semanticweb.owlapi.model.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Component
public class OntologyHelper {

    private int counter = 0;

    private final String uploadDir;

    public void reinitializeCounter() {
        counter = 0;
    }

    public OntologyHelper(AppConfig appConfig) {
        this.uploadDir = appConfig.getUploadDir();
    }

    public Ontology readOntology(String ontologyUrl) throws OWLOntologyCreationException {
        OntologyManager om = OntManagers.createManager();
        return om.loadOntologyFromOntologyDocument(IRI.create(ontologyUrl));

    }

    public Ontology readOntology(File ontologyFile) throws OWLOntologyCreationException {
        OntologyManager om = OntManagers.createManager();
        return om.loadOntologyFromOntologyDocument(ontologyFile);

    }

    public boolean isSimpleClass(OWLClassExpression classExpression) {
        return classExpression.isClassExpressionLiteral();
    }

    public IRI getOntologyUri(Ontology ontology) {
        return ontology.getOntologyID().getOntologyIRI().orElse(null);
    }

    public OWLClass createNewClass(IRI ontologyIRI, DataFactory dataFactory) {
        String iri = "http://example.com/ontology#NewClass_" + counter++;
        return dataFactory.getOWLClass(IRI.create(iri));
    }

    public File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(uploadDir + file.getOriginalFilename());
        file.transferTo(convFile);
        return convFile;
    }

}
