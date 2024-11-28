package fr.uttop.normalization.services;

import com.github.owlcs.ontapi.Ontology;
import org.apache.jena.ontology.Individual;
import org.json.JSONObject;


import fr.uttop.normalization.entities.NormalizedOntology;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class MetricsServiceImpl implements MetricsSerivce{

    public JSONObject getAxiomMetrics(OWLOntology ontology) {
        JSONObject metrics = new JSONObject();

        // Total axiom count
        Long totalAxioms = ontology.axioms().count();
        metrics.put("Axiom", totalAxioms);

        // Logical axiom count (all axioms except annotation axioms)
        Long logicalAxioms = ontology.axioms().filter(a -> !(a instanceof OWLAnnotationAssertionAxiom)).count();
        metrics.put("LogicalAxiomCount", logicalAxioms);

        // Declaration axioms count
        Long declarationAxioms = ontology.axioms(AxiomType.DECLARATION).count();
        metrics.put("DeclarationAxiomsCount", declarationAxioms);

        // Class count
        Long classCount = ontology.classesInSignature().count();
        metrics.put("ClassCount", classCount);

        // Object property count
        Long objectPropertyCount = ontology.objectPropertiesInSignature().count();
        metrics.put("ObjectPropertyCount", objectPropertyCount);

        // Data property count
        Long dataPropertyCount = ontology.dataPropertiesInSignature().count();
        metrics.put("DataPropertyCount", dataPropertyCount);

        // Individual count
        Long individualCount = ontology.individualsInSignature().count();
        metrics.put("IndividualCount", individualCount);

        // Annotation property count
        Long annotationPropertyCount = ontology.annotationPropertiesInSignature().count();
        metrics.put("AnnotationPropertyCount", annotationPropertyCount);

        int totalAxiomCount = ontology.getAxioms().size();
        double axiomComplexity = totalAxiomCount == 0 ? 0 : (double) totalAxiomCount / ontology.getClassesInSignature().size();
        metrics.put("axiomComplexity", axiomComplexity);

        return metrics;
    }

    @Override
    public JSONObject getClassMetrics(NormalizedOntology ontology) {
        Ontology sourceOntology = ontology.getSourceOntology();

        JSONObject classMetrics = new JSONObject();

        Set<OWLClass> classes = sourceOntology.getClassesInSignature();
        int classCount = classes.size();

        Set<OWLNamedIndividual> individuals = sourceOntology.getIndividualsInSignature();
        int individualsCount = individuals.size();

        int rootClassCount = (int) classes.stream()
                .filter(cls -> sourceOntology.getSubClassAxiomsForSuperClass(cls).isEmpty())
                .count();
        int leafClassCount = (int) classes.stream()
                .filter(cls -> sourceOntology.getSubClassAxiomsForSubClass(cls).isEmpty())
                .count();

        Reasoner reasoner = new Reasoner(new Configuration(), sourceOntology);
        List<Integer> depths = classes.stream()
                .map(cls -> getClassDepth(cls, reasoner))
                .filter(depth -> depth > 0) // Exclude unsatisfiable classes
                .toList();
        int maxDepth = depths.isEmpty() ? 0 : Collections.max(depths);
        double avgDepth = depths.isEmpty() ? 0 : depths.stream().mapToDouble(Number::doubleValue).average().orElse(0);

        double classConnectivity = classes.stream()
                .mapToInt(cls -> (int) sourceOntology.getObjectPropertiesInSignature().stream()
                        .filter(prop -> sourceOntology.getObjectPropertyDomainAxioms(prop).stream()
                                .anyMatch(ax -> ax.getDomain().equals(cls)))
                        .count())
                .average().orElse(0);

        classMetrics.put("classCount", classCount);
        classMetrics.put("individualCount", individualsCount);
        classMetrics.put("rootClassCount", rootClassCount);
        classMetrics.put("leafClassCount", leafClassCount);
        classMetrics.put("maxDepth", maxDepth);
        classMetrics.put("avgDepth", avgDepth);
        classMetrics.put("classConnectivity", classConnectivity);

        return classMetrics;

    }

    public JSONObject getClassAxiomMetrics(NormalizedOntology ontology) {
        Ontology sourceOntology = ontology.getSourceOntology();
        JSONObject metrics = new JSONObject();

        // Count subclass axioms
        Long subclassAxioms = sourceOntology.axioms(AxiomType.SUBCLASS_OF).count();
        metrics.put("SubClassAxioms", subclassAxioms);

        // Count equivalent class axioms
        Long equivalentClassAxioms = sourceOntology.axioms(AxiomType.EQUIVALENT_CLASSES).count();
        metrics.put("EquivalentClassAxioms", equivalentClassAxioms);

        // Count disjoint class axioms
        Long disjointClassAxioms = sourceOntology.axioms(AxiomType.DISJOINT_CLASSES).count();
        metrics.put("DisjointClassAxioms", disjointClassAxioms);

        // Count other types of class axioms if needed
        Long classAssertionAxioms = sourceOntology.axioms(AxiomType.CLASS_ASSERTION).count();
        metrics.put("ClassAssertionAxioms", classAssertionAxioms);

        return metrics;
    }

    public JSONObject getObjectPropertyAxiomsMetrics(OWLOntology ontology) {
        JSONObject metrics = new JSONObject();

        // SubObjectPropertyOf axioms
        Long subObjectPropertyAxioms = ontology.axioms(AxiomType.SUB_OBJECT_PROPERTY).count();
        metrics.put("SubObjectPropertyOf", subObjectPropertyAxioms);

        // EquivalentObjectProperties axioms
        Long equivalentObjectPropertiesAxioms = ontology.axioms(AxiomType.EQUIVALENT_OBJECT_PROPERTIES).count();
        metrics.put("EquivalentObjectProperties", equivalentObjectPropertiesAxioms);

        // InverseObjectProperties axioms
        Long inverseObjectPropertiesAxioms = ontology.axioms(AxiomType.INVERSE_OBJECT_PROPERTIES).count();
        metrics.put("InverseObjectProperties", inverseObjectPropertiesAxioms);

        // DisjointObjectProperties axioms
        Long disjointObjectPropertiesAxioms = ontology.axioms(AxiomType.DISJOINT_OBJECT_PROPERTIES).count();
        metrics.put("DisjointObjectProperties", disjointObjectPropertiesAxioms);

        // FunctionalObjectProperty axioms
        Long functionalObjectPropertyAxioms = ontology.axioms(AxiomType.FUNCTIONAL_OBJECT_PROPERTY).count();
        metrics.put("FunctionalObjectProperty", functionalObjectPropertyAxioms);

        // InverseFunctionalObjectProperty axioms
        Long inverseFunctionalObjectPropertyAxioms = ontology.axioms(AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY).count();
        metrics.put("InverseFunctionalObjectProperty", inverseFunctionalObjectPropertyAxioms);

        // TransitiveObjectProperty axioms
        Long transitiveObjectPropertyAxioms = ontology.axioms(AxiomType.TRANSITIVE_OBJECT_PROPERTY).count();
        metrics.put("TransitiveObjectProperty", transitiveObjectPropertyAxioms);

        // SymmetricObjectProperty axioms
        Long symmetricObjectPropertyAxioms = ontology.axioms(AxiomType.SYMMETRIC_OBJECT_PROPERTY).count();
        metrics.put("SymmetricObjectProperty", symmetricObjectPropertyAxioms);

        // AsymmetricObjectProperty axioms
        Long asymmetricObjectPropertyAxioms = ontology.axioms(AxiomType.ASYMMETRIC_OBJECT_PROPERTY).count();
        metrics.put("AsymmetricObjectProperty", asymmetricObjectPropertyAxioms);

        // ReflexiveObjectProperty axioms
        Long reflexiveObjectPropertyAxioms = ontology.axioms(AxiomType.REFLEXIVE_OBJECT_PROPERTY).count();
        metrics.put("ReflexiveObjectProperty", reflexiveObjectPropertyAxioms);

        // IrreflexiveObjectProperty axioms
        Long irreflexiveObjectPropertyAxioms = ontology.axioms(AxiomType.IRREFLEXIVE_OBJECT_PROPERTY).count();
        metrics.put("IrreflexiveObjectProperty", irreflexiveObjectPropertyAxioms);

        // ObjectPropertyDomain axioms
        Long objectPropertyDomainAxioms = ontology.axioms(AxiomType.OBJECT_PROPERTY_DOMAIN).count();
        metrics.put("ObjectPropertyDomain", objectPropertyDomainAxioms);

        // ObjectPropertyRange axioms
        Long objectPropertyRangeAxioms = ontology.axioms(AxiomType.OBJECT_PROPERTY_RANGE).count();
        metrics.put("ObjectPropertyRange", objectPropertyRangeAxioms);

        // SubPropertyChainOf axioms
        Long subPropertyChainOfAxioms = ontology.axioms(AxiomType.SUB_PROPERTY_CHAIN_OF).count();
        metrics.put("SubPropertyChainOf", subPropertyChainOfAxioms);

        return metrics;
    }

    public JSONObject getDataPropertyAxiomsMetrics(OWLOntology ontology) {
        JSONObject metrics = new JSONObject();

        // SubDataPropertyOf axioms
        Long subDataPropertyAxioms = ontology.axioms(AxiomType.SUB_DATA_PROPERTY).count();
        metrics.put("SubDataPropertyOf", subDataPropertyAxioms);

        // EquivalentDataProperties axioms
        Long equivalentDataPropertiesAxioms = ontology.axioms(AxiomType.EQUIVALENT_DATA_PROPERTIES).count();
        metrics.put("EquivalentDataProperties", equivalentDataPropertiesAxioms);

        // DisjointDataProperties axioms
        Long disjointDataPropertiesAxioms = ontology.axioms(AxiomType.DISJOINT_DATA_PROPERTIES).count();
        metrics.put("DisjointDataProperties", disjointDataPropertiesAxioms);

        // FunctionalDataProperty axioms
        Long functionalDataPropertyAxioms = ontology.axioms(AxiomType.FUNCTIONAL_DATA_PROPERTY).count();
        metrics.put("FunctionalDataProperty", functionalDataPropertyAxioms);

        // DataPropertyDomain axioms
        Long dataPropertyDomainAxioms = ontology.axioms(AxiomType.DATA_PROPERTY_DOMAIN).count();
        metrics.put("DataPropertyDomain", dataPropertyDomainAxioms);

        // DataPropertyRange axioms
        Long dataPropertyRangeAxioms = ontology.axioms(AxiomType.DATA_PROPERTY_RANGE).count();
        metrics.put("DataPropertyRange", dataPropertyRangeAxioms);

        return metrics;
    }

    public JSONObject getIndividualAxiomsMetrics(OWLOntology ontology) {
        JSONObject metrics = new JSONObject();

        // ClassAssertion axioms
        Long classAssertionAxioms = ontology.axioms(AxiomType.CLASS_ASSERTION).count();
        metrics.put("ClassAssertion", classAssertionAxioms);

        // ObjectPropertyAssertion axioms
        Long objectPropertyAssertionAxioms = ontology.axioms(AxiomType.OBJECT_PROPERTY_ASSERTION).count();
        metrics.put("ObjectPropertyAssertion", objectPropertyAssertionAxioms);

        // DataPropertyAssertion axioms
        Long dataPropertyAssertionAxioms = ontology.axioms(AxiomType.DATA_PROPERTY_ASSERTION).count();
        metrics.put("DataPropertyAssertion", dataPropertyAssertionAxioms);

        // NegativeObjectPropertyAssertion axioms
        Long negativeObjectPropertyAssertionAxioms = ontology.axioms(AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION).count();
        metrics.put("NegativeObjectPropertyAssertion", negativeObjectPropertyAssertionAxioms);

        // NegativeDataPropertyAssertion axioms
        Long negativeDataPropertyAssertionAxioms = ontology.axioms(AxiomType.NEGATIVE_DATA_PROPERTY_ASSERTION).count();
        metrics.put("NegativeDataPropertyAssertion", negativeDataPropertyAssertionAxioms);

        // SameIndividual axioms
        Long sameIndividualAxioms = ontology.axioms(AxiomType.SAME_INDIVIDUAL).count();
        metrics.put("SameIndividual", sameIndividualAxioms);

        // DifferentIndividuals axioms
        Long differentIndividualsAxioms = ontology.axioms(AxiomType.DIFFERENT_INDIVIDUALS).count();
        metrics.put("DifferentIndividuals", differentIndividualsAxioms);

        return metrics;
    }

    public JSONObject getAnnotationAxiomsMetrics(OWLOntology ontology) {
        JSONObject metrics = new JSONObject();

        // AnnotationAssertion axioms
        Long annotationAssertionAxioms = ontology.axioms(AxiomType.ANNOTATION_ASSERTION).count();
        metrics.put("AnnotationAssertion", annotationAssertionAxioms);

        // AnnotationPropertyDomain axioms
        Long annotationPropertyDomainAxioms = ontology.axioms(AxiomType.ANNOTATION_PROPERTY_DOMAIN).count();
        metrics.put("AnnotationPropertyDomain", annotationPropertyDomainAxioms);

        // AnnotationPropertyRange axioms
        Long annotationPropertyRangeAxioms = ontology.axioms(AxiomType.ANNOTATION_PROPERTY_RANGE).count();
        metrics.put("AnnotationPropertyRange", annotationPropertyRangeAxioms);

        return metrics;
    }

    private static int getClassDepth(OWLClass cls, Reasoner reasoner) {
        return reasoner.getSuperClasses(cls, false).getFlattened().size();
    }

}
