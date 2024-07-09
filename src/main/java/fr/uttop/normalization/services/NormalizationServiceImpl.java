package fr.uttop.normalization.services;

import com.github.owlcs.ontapi.DataFactory;
import com.github.owlcs.ontapi.Ontology;
import com.github.owlcs.ontapi.OntologyManager;
import fr.uttop.normalization.entities.NormalizedOntology;
import fr.uttop.ontoeval.config.AppConfig;
import fr.uttop.ontoeval.helpers.OntologyHelper;
import org.semanticweb.owlapi.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Service
public class NormalizationServiceImpl implements NormalizationService {

    private final String uploadDir;

    @Autowired
    public NormalizationServiceImpl(AppConfig appConfig) {
        this.uploadDir = appConfig.getUploadDir();
    }
    @Autowired
    OntologyHelper ontologyHelper;

    public File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(uploadDir + file.getOriginalFilename());
        file.transferTo(convFile);
        return convFile;
    }

    public NormalizedOntology firstNormalization(NormalizedOntology normalizedOntology) {

        Ontology ontology = normalizedOntology.getSourceOntology();
        Ontology outputOntology = normalizedOntology.getNormalizedOntology();

        OntologyManager manager = normalizedOntology.getManager();
        DataFactory dataFactory = normalizedOntology.getDataFactory();



        Set<OWLAxiom> newAxioms = new HashSet<>();
        Set<OWLAxiom> axiomsToRemove = new HashSet<>();


        simplifyDisjointUnionAxioms(ontology, dataFactory, newAxioms, axiomsToRemove);
        simplifyDisjointAxioms(ontology, dataFactory, newAxioms, axiomsToRemove);
        simplifySubSumptions(ontology, dataFactory, newAxioms, axiomsToRemove);
        simplifyEquivalentClassesAxioms(ontology, dataFactory, newAxioms, axiomsToRemove);

        manager.addAxioms(outputOntology, newAxioms);
        manager.removeAxioms(outputOntology, axiomsToRemove);

        normalizedOntology.setNormalizedOntology(outputOntology);

        return normalizedOntology;

    }

    public void simplifySubSumptions(Ontology ontology, DataFactory dataFactory, Set<OWLAxiom> newAxioms, Set<OWLAxiom> axiomsToRemove ) {
        for (OWLSubClassOfAxiom axiom : ontology.getAxioms(AxiomType.SUBCLASS_OF)) {
            OWLClassExpression subClass = axiom.getSubClass();
            OWLClassExpression superClass = axiom.getSuperClass();

            if (!ontologyHelper.isSimpleClass(subClass) || !ontologyHelper.isSimpleClass(superClass)) {
                axiomsToRemove.add(axiom);

                if (!ontologyHelper.isSimpleClass(subClass)) {
                    OWLClass newClass = ontologyHelper.createNewClass(ontologyHelper.getOntologyUri(ontology), dataFactory);
                    newAxioms.add(dataFactory.getOWLSubClassOfAxiom(newClass, subClass));
                    subClass = newClass;
                }

                if (!ontologyHelper.isSimpleClass(superClass)) {
                    OWLClass newClass = ontologyHelper.createNewClass(ontologyHelper.getOntologyUri(ontology), dataFactory);
                    newAxioms.add(dataFactory.getOWLEquivalentClassesAxiom(newClass, superClass));
                    superClass = newClass;
                }

                newAxioms.add(dataFactory.getOWLSubClassOfAxiom(subClass, superClass));
            }
        }
    }

    public void simplifyDisjointAxioms(Ontology ontology, DataFactory dataFactory, Set<OWLAxiom> newAxioms, Set<OWLAxiom> axiomsToRemove) {
        for (OWLDisjointClassesAxiom axiom : ontology.getAxioms(AxiomType.DISJOINT_CLASSES)) {
            axiomsToRemove.add(axiom);

            Set<OWLClassExpression> classExpressions = axiom.getClassExpressions();
            if (classExpressions.size() == 2) {
                OWLClassExpression[] classes = classExpressions.toArray(new OWLClassExpression[0]);
                OWLClassExpression intersection = dataFactory.getOWLObjectIntersectionOf(classes[0], classes[1]);
                newAxioms.add(dataFactory.getOWLEquivalentClassesAxiom(dataFactory.getOWLNothing(), intersection));
            } else {
                // Handle the case where there are more than two disjoint classes
                OWLClassExpression intersection = dataFactory.getOWLObjectIntersectionOf(classExpressions);
                newAxioms.add(dataFactory.getOWLEquivalentClassesAxiom(dataFactory.getOWLNothing(), intersection));
            }
        }
    }

    public void simplifyDisjointUnionAxioms(Ontology ontology, DataFactory dataFactory, Set<OWLAxiom> newAxioms, Set<OWLAxiom> axiomsToRemove) {
        for (OWLDisjointUnionAxiom axiom : ontology.getAxioms(AxiomType.DISJOINT_UNION)) {
            axiomsToRemove.add(axiom);

            OWLClassExpression mainClass = axiom.getOWLClass();
            Set<OWLClassExpression> classExpressions = axiom.getClassExpressions();

            // EquivalentClasses(C UnionOf(D E ...))
            OWLClassExpression union = dataFactory.getOWLObjectUnionOf(classExpressions);
            newAxioms.add(dataFactory.getOWLEquivalentClassesAxiom(mainClass, union));

            // EquivalentClasses(owl:Nothing IntersectionOf(D E ...))
            OWLClassExpression intersection = dataFactory.getOWLObjectIntersectionOf(classExpressions);
            newAxioms.add(dataFactory.getOWLEquivalentClassesAxiom(dataFactory.getOWLNothing(), intersection));
        }
    }
    public void simplifyEquivalentClassesAxioms(Ontology ontology, DataFactory dataFactory, Set<OWLAxiom> newAxioms, Set<OWLAxiom> axiomsToRemove) {
        for (OWLEquivalentClassesAxiom axiom : ontology.getAxioms(AxiomType.EQUIVALENT_CLASSES)) {
            Set<OWLClassExpression> classExpressions = axiom.getClassExpressions();
            if (classExpressions.size() == 2) {
                OWLClassExpression[] classes = classExpressions.toArray(new OWLClassExpression[0]);
                OWLClassExpression class1 = classes[0];
                OWLClassExpression class2 = classes[1];

                if (!ontologyHelper.isSimpleClass(class1) && !ontologyHelper.isSimpleClass(class2)) {
                    axiomsToRemove.add(axiom);
                    OWLClass newClass = ontologyHelper.createNewClass(ontologyHelper.getOntologyUri(ontology), dataFactory);
                    newAxioms.add(dataFactory.getOWLEquivalentClassesAxiom(newClass, class1));
                    newAxioms.add(dataFactory.getOWLEquivalentClassesAxiom(newClass, class2));
                } else if (!ontologyHelper.isSimpleClass(class1) && ontologyHelper.isSimpleClass(class2)) {
                    axiomsToRemove.add(axiom);
                    newAxioms.add(dataFactory.getOWLEquivalentClassesAxiom(class2, class1));
                } else if (ontologyHelper.isSimpleClass(class1) && !ontologyHelper.isSimpleClass(class2)) {
                    axiomsToRemove.add(axiom);
                    newAxioms.add(dataFactory.getOWLEquivalentClassesAxiom(class1, class2));
                }
            }
        }
    }


}
