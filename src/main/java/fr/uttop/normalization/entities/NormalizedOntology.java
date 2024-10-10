package fr.uttop.normalization.entities;

import com.github.owlcs.ontapi.DataFactory;
import com.github.owlcs.ontapi.OntManagers;
import com.github.owlcs.ontapi.Ontology;
import com.github.owlcs.ontapi.OntologyManager;
import org.semanticweb.owlapi.io.AbstractOWLRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.io.OWLRenderer;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.SimpleRenderer;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class NormalizedOntology {

    private String ontologyUri;

    private Ontology sourceOntology;

    private Ontology normalizedOntology;

    private OntologyManager manager;

    private DataFactory dataFactory;

    public NormalizedOntology(Ontology sourceOntology) {
        this.sourceOntology = sourceOntology;
        this.manager = OntManagers.createManager();
        this.dataFactory = manager.getOWLDataFactory();

        this.ontologyUri = sourceOntology.asGraphModel().getID().toString();

        this.normalizedOntology = this.manager.createOntology();
        this.manager.addAxioms(this.normalizedOntology, this.sourceOntology.getAxioms());

    }

    public OntologyManager getManager() {
        return manager;
    }

    public void setManager(OntologyManager manager) {
        this.manager = manager;
    }

    public DataFactory getDataFactory() {
        return dataFactory;
    }

    public void setDataFactory(DataFactory dataFactory) {
        this.dataFactory = dataFactory;
    }

    public Ontology getSourceOntology() {
        return sourceOntology;
    }

    public void setSourceOntology(Ontology sourceOntology) {
        this.sourceOntology = sourceOntology;
    }

    public String getOntologyUri() {
        return ontologyUri;
    }

    public void setOntologyUri(String ontologyUri) {
        this.ontologyUri = ontologyUri;
    }

    public Ontology getNormalizedOntology() {
        return normalizedOntology;
    }

    public void setNormalizedOntology(Ontology normalizedOntology) {
        this.normalizedOntology = normalizedOntology;
    }


    public String outputString() {
        OutputStream outputStream = new ByteArrayOutputStream();
        try {
            this.manager.saveOntology(normalizedOntology, outputStream);
        } catch (OWLOntologyStorageException e) {
            throw new RuntimeException(e);
        }

        return outputStream.toString();
    }

}
