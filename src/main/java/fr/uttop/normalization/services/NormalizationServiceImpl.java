package fr.uttop.normalization.services;

import com.github.owlcs.ontapi.DataFactory;
import com.github.owlcs.ontapi.Ontology;
import com.github.owlcs.ontapi.OntologyManager;
import com.github.sszuev.jena.ontapi.model.OntIndividual;
import com.github.sszuev.jena.ontapi.vocabulary.RDF;
import fr.uttop.normalization.entities.NormalizedOntology;
import fr.uttop.ontoeval.config.AppConfig;
import fr.uttop.ontoeval.helpers.OntologyHelper;
import com.github.sszuev.jena.ontapi.model.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public NormalizedOntology secondNormalization(NormalizedOntology ontology) {
        OntologyManager manager = ontology.getManager();
        Ontology normalizedOntology = ontology.getNormalizedOntology();
        DataFactory dataFactory = ontology.getDataFactory();

        OntModel model = manager.models().findFirst().orElse(null);

        if (model == null) {
            return ontology;
        }


        for (OntIndividual.Anonymous anon : model.individuals().filter(OntIndividual.Anonymous.class::isInstance).map(OntIndividual.Anonymous.class::cast).toList()) {
            String uri = ontology.getOntologyUri() + "#" + UUID.randomUUID().toString();
            OntIndividual.Named namedIndividual = model.createIndividual(uri);



            anon.types().forEach(type -> {
                model.add(namedIndividual, RDF.type, type);
            });



            // Copy properties from the anonymous individual to the new named individual
            anon.listProperties().forEachRemaining(stmt -> {
                Resource subject = stmt.getSubject();
                if (subject.equals(anon)) {
                    model.add(namedIndividual, stmt.getPredicate(), stmt.getObject());
                } else {
                    model.add(stmt.getSubject(), stmt.getPredicate(), namedIndividual);
                }
            });

            // Remove the anonymous individual
            model.remove(anon.listProperties());
        }

        return ontology;
    }

    @Override
    public NormalizedOntology thirdNormalization(NormalizedOntology ontology) {
        OntologyManager manager = ontology.getManager();
        Ontology normalizedOntology = ontology.getNormalizedOntology();
        DataFactory dataFactory = ontology.getDataFactory();

        Reasoner reasoner = new Reasoner(new Configuration(), normalizedOntology);

        InferredOntologyGenerator generator = new InferredOntologyGenerator(reasoner);
        generator.fillOntology(dataFactory, normalizedOntology);

        Model model = manager.models().findFirst().orElse(null);

        if (model == null) {
            return ontology;
        }

        Set<OWLClass> visited = new HashSet<>();
        Map<Set<OWLClass>, OWLClass> cycleToNewClass = new HashMap<>();

        for (OWLClass cls : normalizedOntology.classesInSignature().collect(Collectors.toSet())) {
            if (!visited.contains(cls)) {
                Set<OWLClass> cycle = detectCycle(cls, reasoner, new HashSet<>(), new HashSet<>());
                if (!cycle.isEmpty()) {
                    OWLClass newClass = createEquivalentClass(cycle, model, ontology.getNormalizedOntology(), dataFactory, ontology.getOntologyUri());
                    cycleToNewClass.put(cycle, newClass);
                    visited.addAll(cycle);
                }
            }
        }

        for (Map.Entry<Set<OWLClass>, OWLClass> entry : cycleToNewClass.entrySet()) {
            Set<OWLClass> cycle = entry.getKey();
            OWLClass newClass = entry.getValue();
            replaceCycleInAxioms(cycle, newClass, normalizedOntology, dataFactory);
        }

        removeRedundantAxioms(normalizedOntology, reasoner);

        normalizeClassNamesInAxioms(normalizedOntology, cycleToNewClass);

        return ontology;
    }


    @Override
    public NormalizedOntology normalizeOntology(NormalizedOntology ontology) {
        firstNormalization(ontology);
        return ontology;
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

    private Set<OWLClass> detectCycle(OWLClass cls, Reasoner reasoner, Set<OWLClass> path, Set<OWLClass> visited) {
        Set<OWLClass> cycle = new HashSet<>();
        if (path.contains(cls)) {
            cycle.add(cls);
            return cycle;
        }

        if (visited.contains(cls)) {
            return cycle;
        }

        path.add(cls);
        visited.add(cls);

        for (Node<OWLClass> superClassNode : reasoner.getSuperClasses(cls, true)) {
            OWLClass superClass = superClassNode.getRepresentativeElement();
            cycle.addAll(detectCycle(superClass, reasoner, path, visited));
        }

        path.remove(cls);
        return cycle;
    }

    private OWLClass createEquivalentClass(Set<OWLClass> cycle, Model model, Ontology ontology, DataFactory dataFactory, String ontologyUri) {
        String uri = ontologyUri + "#" + UUID.randomUUID().toString();
        OWLClass newClass = dataFactory.getOWLClass(IRI.create(uri));

        for (OWLClass cls : cycle) {
            OWLAxiom equivalentAxiom = dataFactory.getOWLEquivalentClassesAxiom(newClass, cls);
            ontology.add(equivalentAxiom);
        }

        return newClass;
    }

    private void replaceCycleInAxioms(Set<OWLClass> cycle, OWLClass newClass, Ontology ontology, DataFactory dataFactory) {
        Set<OWLAxiom> axiomsToRemove = new HashSet<>();
        Set<OWLAxiom> axiomsToAdd = new HashSet<>();

        for (OWLAxiom axiom : ontology.axioms().collect(Collectors.toSet())) {
            Set<OWLClass> axiomClasses = axiom.classesInSignature().collect(Collectors.toSet());
            if (cycle.containsAll(axiomClasses)) {
                axiomsToRemove.add(axiom);
            } else if (cycle.stream().anyMatch(axiomClasses::contains)) {
                OWLAxiom newAxiom = replaceSubClassCyclesInAxiom(axiom, cycle, newClass, dataFactory);
                axiomsToRemove.add(axiom);
                axiomsToAdd.add(newAxiom);
            }
        }

        ontology.remove(axiomsToRemove);
        ontology.add(axiomsToAdd);
    }

    private OWLAxiom replaceSubClassCyclesInAxiom(OWLAxiom axiom, Set<OWLClass> cycle, OWLClass newClass, DataFactory dataFactory) {
        // Implement logic to replace class in the given axiom with newClass
        // This is just a placeholder; actual implementation will depend on the type of axiom
        // For example:
        if (axiom instanceof OWLSubClassOfAxiom) {
            OWLSubClassOfAxiom subClassAxiom = (OWLSubClassOfAxiom) axiom;
            OWLClassExpression subClass = subClassAxiom.getSubClass();
            OWLClassExpression superClass = subClassAxiom.getSuperClass();
            if (cycle.contains(subClass)) {
                subClass = newClass;
            }
            if (cycle.contains(superClass)) {
                superClass = newClass;
            }
            return dataFactory.getOWLSubClassOfAxiom(subClass, superClass);
        }
        // Handle other types of axioms similarly
        return axiom;
    }

    private void removeRedundantAxioms(Ontology ontology, Reasoner reasoner) {
        Set<OWLAxiom> redundantAxioms = new HashSet<>();
        for (OWLAxiom axiom : ontology.axioms().collect(Collectors.toSet())) {
            if (isRedundant(axiom, reasoner)) {
                redundantAxioms.add(axiom);
            }
        }
        ontology.remove(redundantAxioms);
    }

    private boolean isRedundant(OWLAxiom axiom, Reasoner reasoner) {
        if (!(axiom instanceof OWLSubClassOfAxiom)) {
            return false;
        }

        OWLSubClassOfAxiom subClassAxiom = (OWLSubClassOfAxiom) axiom;
        OWLClassExpression subClass = subClassAxiom.getSubClass();
        OWLClassExpression superClass = subClassAxiom.getSuperClass();

        if (!(subClass instanceof OWLClass) || !(superClass instanceof OWLClass)) {
            return false;
        }

        OWLClass subClassC = (OWLClass) subClass;
        OWLClass superClassC = (OWLClass) superClass;

        // Remove the axiom temporarily from the ontology to check redundancy
        reasoner.getRootOntology().removeAxiom(axiom);

        boolean isRedundant = reasoner.getSubClasses(superClassC, false).containsEntity(subClassC);

        // Add the axiom back to the ontology
        reasoner.getRootOntology().addAxiom(axiom);

        return isRedundant;
    }

    private void normalizeClassNamesInAxioms(Ontology ontology, Map<Set<OWLClass>, OWLClass> cycleToNewClass) {
        // Implement logic to normalize class names in various axioms
        // This is just a placeholder; actual implementation will depend on the type of axiom
    }


}

