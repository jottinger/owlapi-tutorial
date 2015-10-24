package com.autumncode.owlapi.ontology;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.vocab.OWLFacet;
import org.testng.Assert;
import org.testng.annotations.Test;
import uk.ac.manchester.cs.jfact.JFactFactory;

import java.util.*;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

public class ClassTest {
    @Test
    public void testAddClassesSimple() throws OWLOntologyCreationException, OWLOntologyStorageException {
        OntologyHelper oh = new OntologyHelper();
        OWLOntology o = oh.createOntology("http://autumncode.com/ontologies/person.owl");
        OWLClass person = oh.createClass("http://autumncode.com/ontologies/person.owl#Person");
        OWLClass fireman = oh.createClass("http://autumncode.com/ontologies/person.owl#Fireman");
        OWLAxiomChange axiom = oh.createSubclass(o, fireman, person);
        oh.applyChange(axiom);

        Set<String> superclasses = o.getSubClassAxiomsForSubClass(fireman)
                .stream()
                .map(ax -> ax.getSuperClass().asOWLClass().toStringID())
                .collect(Collectors.toSet());
        assertEquals(1, superclasses.size());
        assertTrue(superclasses.contains(person.toStringID()));
    }

    @Test
    public void testAddClassesMoreComplex() throws OWLOntologyCreationException, OWLOntologyStorageException {
        OntologyHelper oh = new OntologyHelper();
        OWLOntology o = oh.createOntology("http://autumncode.com/ontologies/terminator.owl");
        OWLClass person = oh.createClass("http://autumncode.com/ontologies/terminator.owl#Person");
        OWLClass robot = oh.createClass("http://autumncode.com/ontologies/terminator.owl#Robot");
        OWLClass terminator = oh.createClass("http://autumncode.com/ontologies/terminator.owl#Terminator");

        oh.applyChange(oh.createSubclass(o, terminator, person),
                oh.createSubclass(o, terminator, robot));

        Set<String> superclasses = o.getSubClassAxiomsForSubClass(terminator)
                .stream()
                .map(ax -> ax.getSuperClass().asOWLClass().toStringID())
                .collect(Collectors.toSet());
        assertEquals(2, superclasses.size());
        assertTrue(superclasses.contains(person.toStringID()));
        assertTrue(superclasses.contains(robot.toStringID()));
    }

    @Test
    public void addSimpleIndividual() throws Exception {
        OntologyHelper oh = new OntologyHelper();
        OWLOntology o = oh.createOntology("http://autumncode.com/ontologies/person.owl");
        OWLClass person = oh.createClass("http://autumncode.com/ontologies/person.owl#Person");
        OWLIndividual captainKirk = oh.createIndividual("http://autumncode.com/ontologies/person.owl#Kirk");
        oh.applyChange(oh.associateIndividualWithClass(o, person, captainKirk));
        final OWLReasoner reasoner = new JFactFactory().createReasoner(o);
        try (final AutoCloseable ignored = reasoner::dispose) {
            ListMultimap<String, String> classInstanceMap = ArrayListMultimap.create();
            o.getClassesInSignature()
                    .stream()
                    .forEach(clazz ->
                            reasoner.getInstances(clazz, true)
                                    .getFlattened()
                                    .stream()
                                    .forEach(i ->
                                            classInstanceMap.put(clazz.toStringID(), i.toStringID())));
            // should have one class
            assertEquals(classInstanceMap.keySet().size(), 1);
            Set<String> people = new HashSet<>();
            people.add(captainKirk.toStringID());
            assertEquals(classInstanceMap.asMap().get(person.toStringID()), people);
        }
    }

    @Test
    public void addTerminatorIndividuals() throws Exception {
        OntologyHelper oh = new OntologyHelper();
        OWLOntology o = oh.createOntology("http://autumncode.com/ontologies/terminator.owl");
        OWLClass person = oh.createClass("http://autumncode.com/ontologies/terminator.owl#Person");
        OWLClass robot = oh.createClass("http://autumncode.com/ontologies/terminator.owl#Robot");
        OWLClass terminator = oh.createClass("http://autumncode.com/ontologies/terminator.owl#Terminator");

        oh.applyChange(oh.createSubclass(o, terminator, person),
                oh.createSubclass(o, terminator, robot));

        OWLIndividual sarah = oh.createIndividual("http://autumncode.com/ontologies/terminator.owl#Sarah");
        OWLIndividual tank = oh.createIndividual("http://autumncode.com/ontologies/terminator.owl#Tank");
        OWLIndividual t800 = oh.createIndividual("http://autumncode.com/ontologies/terminator.owl#T800");
        oh.applyChange(oh.associateIndividualWithClass(o, person, sarah),
                oh.associateIndividualWithClass(o, robot, tank),
                oh.associateIndividualWithClass(o, terminator, t800));

        final OWLReasoner reasoner = new JFactFactory().createReasoner(o);
        try (final AutoCloseable ignored = reasoner::dispose) {
            ListMultimap<String, String> classInstanceMap = ArrayListMultimap.create();
            o.getClassesInSignature()
                    .stream()
                    .forEach(clazz ->
                            reasoner.getInstances(clazz, false)
                                    .getFlattened()
                                    .stream()
                                    .forEach(i ->
                                            classInstanceMap.put(clazz.toStringID(), i.toStringID())));

            // should have three classes
            assertEquals(classInstanceMap.keySet().size(), 3);

            Set<String> people = new HashSet<>(Arrays.asList(new String[]{sarah.toStringID(), t800.toStringID()}));
            Set<String> robots = new HashSet<>(Arrays.asList(new String[]{t800.toStringID(), tank.toStringID()}));
            Set<String> terminators = new HashSet<>(Arrays.asList(new String[]{t800.toStringID()}));

            assertEquals(new HashSet<>(classInstanceMap.asMap().get(person.toStringID())), people);
            assertEquals(new HashSet<>(classInstanceMap.asMap().get(robot.toStringID())), robots);
            assertEquals(new HashSet<>(classInstanceMap.asMap().get(terminator.toStringID())), terminators);
        }
    }

    @Test
    public void simpleParentage() throws Exception {
        OntologyHelper oh = new OntologyHelper();
        OWLOntology o = oh.createOntology("http://autumncode.com/ontologies/genealogy.owl");
        OWLClass human = oh.createClass("http://autumncode.com/ontologies/geneaology.owl#Human");
        OWLClass male = oh.createClass("http://autumncode.com/ontologies/genealogy.owl#Male");
        OWLClass female = oh.createClass("http://autumncode.com/ontologies/genealogy.owl#Female");
        OWLObjectProperty hasFather =
                oh.createObjectProperty("http://autumncode.com/ontologies/genealogy.owl#hasFather");
        OWLObjectProperty hasMother =
                oh.createObjectProperty("http://autumncode.com/ontologies/genealogy.owl#hasMother");
        oh.applyChange(
                oh.createSubclass(o, male, human),
                oh.createSubclass(o, female, human),
                oh.addDisjointClass(o, female, male),
                oh.addDisjointClass(o, male, female),
                oh.associateObjectPropertyWithClass(o, hasFather, human, male),
                oh.associateObjectPropertyWithClass(o, hasMother, human, female)
        );

        OWLIndividual barry = oh.createIndividual("http://autumncode.com/ontologies/genealogy.owl#barry");
        OWLIndividual shirley = oh.createIndividual("http://autumncode.com/ontologies/genealogy.owl#shirley");
        OWLIndividual thomas = oh.createIndividual("http://autumncode.com/ontologies/genealogy.owl#thomas");
        OWLIndividual michael = oh.createIndividual("http://autumncode.com/ontologies/genealogy.owl#michael");
        OWLIndividual vicki = oh.createIndividual("http://autumncode.com/ontologies/genealogy.owl#vicki");
        OWLIndividual joseph = oh.createIndividual("http://autumncode.com/ontologies/genealogy.owl#joseph");
        OWLIndividual mary = oh.createIndividual("http://autumncode.com/ontologies/genealogy.owl#mary");
        OWLIndividual samuel = oh.createIndividual("http://autumncode.com/ontologies/genealogy.owl#samuel");
        OWLIndividual andrew = oh.createIndividual("http://autumncode.com/ontologies/genealogy.owl#andrew");
        OWLIndividual jonathan = oh.createIndividual("http://autumncode.com/ontologies/genealogy.owl#jonathan");

        oh.applyChange(
                oh.associateIndividualWithClass(o, male, barry),
                oh.associateIndividualWithClass(o, male, thomas),
                oh.associateIndividualWithClass(o, male, michael),
                oh.associateIndividualWithClass(o, male, joseph),
                oh.associateIndividualWithClass(o, male, samuel),
                oh.associateIndividualWithClass(o, male, andrew),
                oh.associateIndividualWithClass(o, male, jonathan),
                oh.associateIndividualWithClass(o, female, shirley),
                oh.associateIndividualWithClass(o, female, vicki),
                oh.associateIndividualWithClass(o, female, mary),
                oh.addObjectproperty(o, michael, hasMother, shirley),
                oh.addObjectproperty(o, michael, hasFather, thomas),
                oh.addObjectproperty(o, vicki, hasMother, shirley),
                oh.addObjectproperty(o, vicki, hasFather, thomas),
                oh.addObjectproperty(o, joseph, hasMother, shirley),
                oh.addObjectproperty(o, joseph, hasFather, barry),
                oh.addObjectproperty(o, andrew, hasMother, mary),
                oh.addObjectproperty(o, andrew, hasFather, samuel),
                oh.addObjectproperty(o, jonathan, hasMother, vicki),
                oh.addObjectproperty(o, jonathan, hasFather, andrew)
        );

        OWLReasoner reasoner = new JFactFactory().createReasoner(o);
        try (final AutoCloseable ignored = reasoner::dispose) {
            assertTrue(reasoner.isConsistent());
        }
        oh.applyChange(oh.associateIndividualWithClass(o, female, jonathan));
        reasoner = new JFactFactory().createReasoner(o);
        try (final AutoCloseable ignored = reasoner::dispose) {
            assertFalse(reasoner.isConsistent());
        }
    }

    @Test
    public void addDataToIndividual() throws Exception {
        OntologyHelper oh = new OntologyHelper();
        OWLOntology o = oh.createOntology("http://autumncode.com/ontologies/terminator.owl");
        OWLClass terminator = oh.createClass("http://autumncode.com/ontologies/terminator.owl#Terminator");
        OWLDataProperty killsHumans =
                oh.createDataProperty("http://autumncode.com/ontologies/terminator.owl#killsHumans");
        OWLIndividual t800 = oh.createIndividual("http://autumncode.com/ontologies/terminator.owl#t800");
        OWLIndividual pops = oh.createIndividual("http://autumncode.com/ontologies/terminator.owl#pops");
        oh.applyChange(
                oh.associateIndividualWithClass(o, terminator, pops),
                oh.associateIndividualWithClass(o, terminator, t800),
                oh.addDataToIndividual(o, t800, killsHumans, true),
                oh.addDataToIndividual(o, pops, killsHumans, false)
        );
    }
}
