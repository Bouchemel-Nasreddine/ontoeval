package fr.uttop.ontoeval.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/")
@RestController
public class RootController {

    @GetMapping("")
    public String index() {
        return "Welcome to OntoEval, an ontology evaluation project!";
    }

}
