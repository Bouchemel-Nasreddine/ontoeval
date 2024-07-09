package fr.uttop.ontoeval.helpers;

import com.github.owlcs.ontapi.DataFactory;
import com.github.owlcs.ontapi.OntManagers;
import com.github.owlcs.ontapi.Ontology;
import com.github.owlcs.ontapi.OntologyManager;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.io.OWLRendererIOException;
import org.semanticweb.owlapi.model.*;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Component
public class OntologyHelper {

    private int counter = 0;

    public void reinitializeCounter() {
        counter = 0;
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

}
