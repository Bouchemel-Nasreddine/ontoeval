package fr.uttop.normalization.controller;


import fr.uttop.normalization.dto.NormalizationDTO;
import fr.uttop.normalization.entities.NormalizedOntology;
import fr.uttop.normalization.services.MetricsSerivce;
import fr.uttop.ontoeval.helpers.OntologyHelper;
import org.json.JSONObject;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@RequestMapping("/metrics")
@RestController()
public class MetricsController {

    @Autowired
    MetricsSerivce metricsSerivce;

    @Autowired
    OntologyHelper ontologyHelper;

    @GetMapping("/test")
    public String index() {
        return "Welcome from metrics";
    }


    @PostMapping("")
    public ResponseEntity<Map<String, Object>> ontologyMetrics(NormalizationDTO request ) {

        String ontologyUrl = request.getOntologyUrl();
        MultipartFile ontologyFile = request.getOntologyFile();
        NormalizedOntology ontology;

        if (ontologyFile != null) {
            // Process the ontology file
            try {
                File file = ontologyHelper.convertMultiPartToFile(ontologyFile);
                ontology = new NormalizedOntology(ontologyHelper.readOntology(file));

            } catch (IOException | OWLOntologyCreationException e) {
                e.printStackTrace();
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else if (ontologyUrl != null && !ontologyUrl.isEmpty()) {
            // Process the ontology URL
            try {
                ontology = new NormalizedOntology(ontologyHelper.readOntology(ontologyUrl));
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        JSONObject axiomsMetrics = metricsSerivce.getAxiomMetrics(ontology.getSourceOntology());
        JSONObject classMetrics = metricsSerivce.getClassMetrics(ontology);
        JSONObject classAxiomsMetrics = metricsSerivce.getClassAxiomMetrics(ontology);
        JSONObject objProbAxiomsMetrics = metricsSerivce.getObjectPropertyAxiomsMetrics(ontology.getSourceOntology());
        JSONObject dataProbAxiomsMetrics = metricsSerivce.getDataPropertyAxiomsMetrics(ontology.getSourceOntology());
        JSONObject indivAxiomsMetrics = metricsSerivce.getIndividualAxiomsMetrics(ontology.getSourceOntology());
        JSONObject annotationsAxiomsMetrics = metricsSerivce.getAnnotationAxiomsMetrics(ontology.getSourceOntology());

        JSONObject metrics = new JSONObject();
        metrics.put("class metrics", classMetrics);
        metrics.put("axiom metrics", axiomsMetrics);
        metrics.put("class axioms metrics", classAxiomsMetrics);
        metrics.put("object property axioms metrics", objProbAxiomsMetrics);
        metrics.put("data property axioms metrics", dataProbAxiomsMetrics);
        metrics.put("individuals axioms metrics", indivAxiomsMetrics);
        metrics.put("annotations axioms metrics", annotationsAxiomsMetrics);
        System.out.println("output metrics: " + metrics);
        ontologyHelper.reinitializeCounter();
        return new ResponseEntity<>(metrics.toMap(), HttpStatus.OK);

    }



}
