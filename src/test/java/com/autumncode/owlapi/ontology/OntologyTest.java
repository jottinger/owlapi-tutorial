package com.autumncode.owlapi.ontology;

import org.semanticweb.owlapi.io.*;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class OntologyTest {
    @Test
    public void createOntology() throws OWLException {
        OntologyHelper oh = new OntologyHelper();
        IRI iri = oh.convertStringToIRI("http://autumncode.com/ontologies/2015/example.owl");
        OWLOntology ontology = oh.createOntology(iri);

        assertNotNull(ontology);
        assertEquals(iri,
                ontology.getOntologyID().getOntologyIRI().or(oh.convertStringToIRI("false")));
    }

    @DataProvider
    Object[][] readDataProvider() {
        return new Object[][]{{
                new StreamDocumentSource(this.getClass().getResourceAsStream("/example1.owl")),
                "http://autumncode.com/ontologies/2015/example1.owl"
        }};
    }

    @Test(dataProvider = "readDataProvider")
    public void readOntology(OWLOntologyDocumentSource source, String baseIRI) throws OWLException {
        OntologyHelper oh = new OntologyHelper();
        OWLOntology ontology = oh.readOntology(source);
        assertNotNull(ontology);
        assertEquals(oh.convertStringToIRI(baseIRI),
                ontology.getOntologyID().getOntologyIRI().or(oh.convertStringToIRI("false")));
    }

    @Test
    public void writeOntology() throws OWLException {
        OntologyHelper oh = new OntologyHelper();
        OWLOntology ontology = oh.createOntology("http://autumncode.com/ontologies/2015/example.owl");
        StringDocumentTarget sdt = new StringDocumentTarget();
        oh.writeOntology(ontology, sdt);

        StringDocumentSource sds = new StringDocumentSource(sdt.toString());
        OWLOntology o = oh.readOntology(sds);
        assertEquals(oh.convertStringToIRI("http://autumncode.com/ontologies/2015/example.owl"),
                o.getOntologyID().getOntologyIRI().or(oh.convertStringToIRI("false")));
    }
}
