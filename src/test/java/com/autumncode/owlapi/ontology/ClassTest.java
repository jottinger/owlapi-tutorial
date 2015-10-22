package com.autumncode.owlapi.ontology;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.testng.annotations.Test;
import uk.ac.manchester.cs.jfact.JFactFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertTrue;

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
            Set<String> people=new HashSet<>();
            people.add(captainKirk.toStringID());
            assertEquals(classInstanceMap.asMap().get(person.toStringID()), people);
        }
        StringDocumentTarget sdt = new StringDocumentTarget();
        oh.writeOntology(o, sdt);
        System.out.println(sdt);
    }
}
