package fr.uttop.normalization.controller;

import com.github.owlcs.ontapi.Ontology;
import fr.uttop.normalization.dto.NormalizationDTO;
import fr.uttop.normalization.entities.NormalizedOntology;
import fr.uttop.normalization.services.NormalizationService;
import fr.uttop.ontoeval.helpers.OntologyHelper;
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

@RequestMapping("/normalization")
@RestController()
public class NormalizationController {

    @Autowired
    NormalizationService normalizationService;

    @Autowired
    OntologyHelper ontologyHelper;

    @GetMapping("")
    public String index() {
        return "Welcome from normalization";
    }

    @PostMapping("")
    public ResponseEntity<String> normalizeOntology(NormalizationDTO request ) {
        String ontologyUrl = request.getOntologyUrl();
        MultipartFile ontologyFile = request.getOntologyFile();
        if (ontologyFile != null) {
            // Process the ontology file
            try {
                File file = ontologyHelper.convertMultiPartToFile(ontologyFile);
                NormalizedOntology ontology = new NormalizedOntology(ontologyHelper.readOntology(file));
                normalizationService.normalizeOntology(ontology);
                System.out.println("output ontology: " + ontology.outputString());
                ontologyHelper.reinitializeCounter();
                return new ResponseEntity<>(ontology.outputString(), HttpStatus.OK);
            } catch (IOException e) {
                e.printStackTrace();
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (OWLOntologyCreationException e) {
                e.printStackTrace();
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else if (ontologyUrl != null && !ontologyUrl.isEmpty()) {
            // Process the ontology URL
            try {
                NormalizedOntology ontology = new NormalizedOntology(ontologyHelper.readOntology(ontologyUrl));
                return new ResponseEntity<>(null,  HttpStatus.OK);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/first")
    public ResponseEntity<String> firstNormalizeOntology(NormalizationDTO request ) {
        String ontologyUrl = request.getOntologyUrl();
        MultipartFile ontologyFile = request.getOntologyFile();
        if (ontologyFile != null) {
            // Process the ontology file
            try {
                File file = ontologyHelper.convertMultiPartToFile(ontologyFile);
                NormalizedOntology ontology = new NormalizedOntology(ontologyHelper.readOntology(file));
                normalizationService.firstNormalization(ontology);
                System.out.println("output ontology: " + ontology.outputString());
                ontologyHelper.reinitializeCounter();
                return new ResponseEntity<>(ontology.outputString(), HttpStatus.OK);
            } catch (IOException e) {
                e.printStackTrace();
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (OWLOntologyCreationException e) {
                e.printStackTrace();
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else if (ontologyUrl != null && !ontologyUrl.isEmpty()) {
            // Process the ontology URL
            try {
                NormalizedOntology ontology = new NormalizedOntology(ontologyHelper.readOntology(ontologyUrl));
                return new ResponseEntity<>(null,  HttpStatus.OK);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/second")
    public ResponseEntity<String> secondNormalizeOntology(NormalizationDTO request ) {
        String ontologyUrl = request.getOntologyUrl();
        MultipartFile ontologyFile = request.getOntologyFile();
        if (ontologyFile != null) {
            // Process the ontology file
            try {
                File file = ontologyHelper.convertMultiPartToFile(ontologyFile);
                NormalizedOntology ontology = new NormalizedOntology(ontologyHelper.readOntology(file));
                normalizationService.secondNormalization(ontology);
                System.out.println("output ontology: " + ontology.outputString());
                ontologyHelper.reinitializeCounter();
                return new ResponseEntity<>(ontology.outputString(), HttpStatus.OK);
            } catch (IOException e) {
                e.printStackTrace();
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (OWLOntologyCreationException e) {
                e.printStackTrace();
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else if (ontologyUrl != null && !ontologyUrl.isEmpty()) {
            // Process the ontology URL
            try {
                NormalizedOntology ontology = new NormalizedOntology(ontologyHelper.readOntology(ontologyUrl));
                return new ResponseEntity<>(null,  HttpStatus.OK);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/third")
    public ResponseEntity<String> thirdNormalizeOntology(NormalizationDTO request ) {
        String ontologyUrl = request.getOntologyUrl();
        MultipartFile ontologyFile = request.getOntologyFile();
        if (ontologyFile != null) {
            // Process the ontology file
            try {
                File file = ontologyHelper.convertMultiPartToFile(ontologyFile);
                NormalizedOntology ontology = new NormalizedOntology(ontologyHelper.readOntology(file));
                normalizationService.thirdNormalization(ontology);
                System.out.println("output ontology: " + ontology.outputString());
                ontologyHelper.reinitializeCounter();
                return new ResponseEntity<>(ontology.outputString(), HttpStatus.OK);
            } catch (IOException e) {
                e.printStackTrace();
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (OWLOntologyCreationException e) {
                e.printStackTrace();
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else if (ontologyUrl != null && !ontologyUrl.isEmpty()) {
            // Process the ontology URL
            try {
                NormalizedOntology ontology = new NormalizedOntology(ontologyHelper.readOntology(ontologyUrl));
                return new ResponseEntity<>(null,  HttpStatus.OK);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

}
