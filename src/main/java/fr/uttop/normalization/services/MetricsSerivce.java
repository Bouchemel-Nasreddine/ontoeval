package fr.uttop.normalization.services;

import fr.uttop.normalization.entities.NormalizedOntology;
import org.json.JSONObject;
import org.semanticweb.owlapi.model.OWLOntology;

public interface MetricsSerivce {

    public JSONObject getClassMetrics(NormalizedOntology ontology);
    public JSONObject getClassAxiomMetrics(NormalizedOntology ontology);
    public JSONObject getObjectPropertyAxiomsMetrics(OWLOntology ontology);
    public JSONObject getDataPropertyAxiomsMetrics(OWLOntology ontology);
    public JSONObject getIndividualAxiomsMetrics(OWLOntology ontology);
    public JSONObject getAnnotationAxiomsMetrics(OWLOntology ontology);
    public JSONObject getAxiomMetrics(OWLOntology ontology);

}
