# An Incomplete Introduction to OWLAPI

OWL is a language used to describe ontologies - systems of knowledge. It's found primarily in the 
[Semantic Web](https://en.wikipedia.org/wiki/Semantic_Web), which mostly means "hardly anywhere," and part of the 
problem is that *using* OWL is really pretty difficult. You can find plenty of technical language, but very 
little that describes how and why you'd actually use this stuff in the real world.

As a result, finding real-world uses is difficult, and where it *is* used in the real world, the usage is 
inconsistent.

That's a shame. OWL can be very useful; you can use it to figure out things, you can use it for actual classification
and data storage. 

However, the purpose of this tutorial is, sadly, not to address the "why" but the "how" of OWL, specifically
focusing on the underlying library used for one of the best-known OWL projects, 
[Protégé](http://protege.stanford.edu/). That library is known as [OWLAPI](https://github.com/owlcs/owlapi).

There are tutorials - sort of - that focus on OWLAPI, and the best of them (meaning: "most popular," the one that
gets thrown at you if you ask for resources) is a [set of slides](http://owlapi.sourceforge.net/owled2011_tutorial.pdf) 
in PDF form, from a presentation at OWLED 2011.

The slides are useful, but also need the presentation content, which is *not* included. The slides are also based
on an old revision of the project, so more recent users not only have to figure out *why* some of the slides 
are included, but how to use the code snippets in a more recent version of the library.

I'd like to change that, if I can.

## Expectations

After this tutorial is finished, I expect readers to be able to create, read, and write ontologies, with a basic 
understanding of what the various common elements in ontologies are and how they're used. 

I do not expect readers to necessarily be left with a complete understanding of OWL, or how it should be used in 
their organization or projects.

I do not expect to be able to accurately or completely describe every nuance of every bit of code I describe. 
Much of OWLAPI programming involves [cargo cult programming](https://en.wikipedia.org/wiki/Cargo_cult_programming): 
"I did this, it worked, therefore I do it every time." I'm afraid that I don't know how to get around this, 
but trying to limit the cargo cult mentality is partially why I'm trying to write this tutorial in the first place.

## Requirements

I am using:

* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html). This is the current live, 
supported version of Java; it's also the *oldest* supported version of Java as of this writing (Java 7 has been 
end-of-lifed) and I don't see the point of using an outmoded version of Java. I actually personally prefer 
[Scala](http://www.scala-lang.org/), and OWLAPI is verbose enough that Scala actually makes the resulting code 
*far* cleaner than it would be in Java, but I think it's more important to be precise (as opposed to concise) 
when writing tutorials.
* [Maven](https://maven.apache.org/). This is my preferred building environment. There's nothing wrong 
with [Gradle](http://gradle.org/), et al; I just prefer Maven as I think it's pretty much the lowest common 
denominator of functional build systems. Everyone has it, everyone can cater to it.
* [TestNG](http://testng.org/doc/index.html). I will be writing most of the code snippets as tests first. 
[JUnit](http://junit.org/) is arguably more "lowest common denominator" than TestNG is, but I find that TestNG 
works better. It's my tutorial, I'll use the test framework I want to use.

My project file for Maven is pretty simple, and isn't likely to change much. This is not a Maven tutorial, so 
here it is in all its raw glory:
 
    <?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>

        <groupId>com.autumncode</groupId>
        <artifactId>owlapi-tutorial</artifactId>
        <version>1.0-SNAPSHOT</version>

        <dependencyManagement>
            <dependencies>
                <dependency>
                    <groupId>net.sourceforge.owlapi</groupId>
                    <artifactId>owlapi-distribution</artifactId>
                    <version>[4.0.2,)</version>
                </dependency>
                <dependency>
                    <groupId>net.sourceforge.owlapi</groupId>
                    <artifactId>owlapi-rio</artifactId>
                    <version>[4.0.2,)</version>
                </dependency>
                <dependency>
                    <groupId>net.sourceforge.owlapi</groupId>
                    <artifactId>jfact</artifactId>
                    <version>4.0.2</version>
                </dependency>
                <dependency>
                    <groupId>org.slf4j</groupId>
                    <artifactId>slf4j-simple</artifactId>
                    <version>1.7.7</version>
                </dependency>
                <dependency>
                    <groupId>org.testng</groupId>
                    <artifactId>testng</artifactId>
                    <version>[6.9.8,)</version>
                    <scope>test</scope>
                </dependency>
            </dependencies>
        </dependencyManagement>

        <dependencies>
            <dependency>
                <groupId>net.sourceforge.owlapi</groupId>
                <artifactId>owlapi-distribution</artifactId>
            </dependency>
            <dependency>
                <groupId>net.sourceforge.owlapi</groupId>
                <artifactId>owlapi-rio</artifactId>
            </dependency>
            <dependency>
                <groupId>org.slf4j</groupId>
                <artifactId>slf4j-simple</artifactId>
            </dependency>
            <dependency>
                <groupId>org.testng</groupId>
                <artifactId>testng</artifactId>
            </dependency>
            <dependency>
                <groupId>net.sourceforge.owlapi</groupId>
                <artifactId>jfact</artifactId>
            </dependency>
        </dependencies>

        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.3</version>
                    <configuration>
                        <source>1.8</source>
                        <target>1.8</target>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </project>

There are a few dependencies that relate to OWLAPI: `owlapi-distribution`, which is 
OWLAPI itself; `owlapi-rio`, which has some classes we need in order to parse and write
OWL; lastly, there's `jfact`, which is a reasoner implementation for OWL. (A reasoner
is used to pull various types of information from OWL.)

## Terms

* An "Ontology" is a system of knowledge. It is composed of descriptions of what things are as well as a set of things.

* A "Class" is a description of what a thing is. A "Person," for example, is a term you might use to describe a 
living being of a specific species (*homo sapiens*, generally), with gender and a name. Therefore, we might describe
a "Person class".

A class can have attributes (a concept familiar to Java coders); these are "properties," which can be made of 
data or references (again, just like Java). Properties can have ranges or defined subsets of values.

A class can also have the concept of a superclass (again, just like Java): a Fireman class might be a valid subclass 
of Person. However, there is no limit to the number of superclasses; a Fireman might be a subclass of the Person, PublicServant, WearsRed, and DrivesTruck classes. Java can model some of this with interfaces, but it's not an exact analogy.

An "Individual" is a concrete instance of a class - much like an "instance," in Java parlance. "John" might be a 
fireman, which implies that he is a person; Sam might be a person (but not a fireman). Sam might have a dog, Fritz, 
who is *not* a Person (instead, is a Dog). All three would be individuals, with varying properties.

# Working with OWLAPI

OWL uses the concept of an "IRI," an "Internationalized Resource Identifier." It's a lot like a URI, but has a wider
set of characters available to it; for the sake of clarity we'll stay seven-bit-clean. 

Basically, an IRI is a unique identifier for a concept, whether it's the ontology itself, a class in the ontology, a 
data property, an individual, or any other unique concept represented in the ontology.
 
OWLAPI creates references to elements in an Ontology by asking a data factory for a reference of a specific type 
with a specific IRI. Relationships between elements are expressed as "axioms." Therefore, the typical interaction with
OWLAPI will look something like this:

1. Create an initial reference with a unique IRI.
1. Create another reference, with a different (and unique) IRI.
1. Express an axiom using the two references.
1. Apply the axiom as a change to the ontology.

## Creating an Ontology

Before we can do anything with an ontology, we need to be able to create one. We're going to create a stateful helper
class in Java to serve as a simple delegate to OWLAPI; it will retain some simple references that we'll end up using
over and over again (the `OWLDataFactory` and `OWLOntologyManager`) and also provide a class into which we can place
simple utility methods.

Many of the methods in our utility class (which I've decided will be called `OntologyHelper` are one-liners, 
or close to it, especially early on.

The baseline for our `OntologyHelper` looks like this, with packages and all imports stripped out:

    public class OntologyHelper {
        OWLOntologyManager m= OWLManager.createOWLOntologyManager();
        OWLDataFactory df=OWLManager.getOWLDataFactory();
    }

We know we'll be converting between `java.lang.String` and `org.semanticweb.owlapi.model.IRI` quite a bit, so we'll
add a conversion method:

    public class OntologyHelper {
        OWLOntologyManager m= OWLManager.createOWLOntologyManager();
        OWLDataFactory df=OWLManager.getOWLDataFactory();

        public IRI convertStringToIRI(String ns) {
            return IRI.create(ns);
        }
    }
    
Now let's look at actually creating an `OWLOntology`. First, let's write a test that creates the ontology and then
checks to make sure the ontology conforms to what we think it should be:

    @Test
    public void createOntology() throws OWLException {
        OntologyHelper oh = new OntologyHelper();
        IRI iri = oh.convertStringToIRI("http://autumncode.com/ontologies/2015/example.owl");
        OWLOntology ontology = oh.createOntology(iri);
    
        assertNotNull(ontology);
        assertEquals(iri,
                ontology.getOntologyID().getOntologyIRI().or(oh.convertStringToIRI("false")));
    }

That last `assertEquals` is a bit scary. The first argument is the IRI we expect the ontology to have; the second 
argument is a work of art. Basically, we're getting the ontology ID, part of which is the ontology's IRI; however,
an ontology might not have an IRI, so it's actually returned in a Java `Optional<IRI>`. We use `.or()` to specify
a constant value (an `IRI`) to use just in case our ontology does not have an `IRI`.

Creating an ontology, given our `m` and `df` declarations from `OntologyHelper`, is a matter of calling 
`m.createOntology` with an IRI. Since working with `String` is a lot more easily conceived of than working with an 
`IRI`, we'll create a convenience method to take a `String` argument, convert it to an `IRI`, and then delegate 
to another method to create the `OWLOntology` itself.

With that, then, let's add some content to our `OntologyHelper`, consisting of the following two methods (only 
one of which is used by the test, since we pass in an `IRI` directly). 

    public OWLOntology createOntology(String iri) throws OWLOntologyCreationException {
        return createOntology(convertStringToIRI(iri));
    }

    public OWLOntology createOntology(IRI iri) throws OWLOntologyCreationException {
        return m.createOntology(iri);
    }

## Writing an Ontology

Writing an ontology is fairly simple. To write an ontology, we simply create a `OWLOntologyDocumentTarget` of some 
kind - examples include a `StringDocumentTarget` and a `FileDocumentTarget`, among others - and then call 
`m.saveOntology()`, supplying the ontology to save, the target, and (optionally) a format to use. (OWL has multiple 
formats, including OWL itself, RDF, Turtle, and Manchester format among others; the formats are out of scope for this
tutorial. We're sticking to the default OWL format.)

First, let's write our test:

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
    
Here, we're creating our ontology (just as we did earlier), but we're writing it into a `StringDocumentTarget`. We
then use that `StringDocumentTarget` to demonstrate reading the ontology, using the read content to validate the
ontology ID.

## Reading an Ontology

Now let's show a little more about reading an ontology - and when we say "a little more," we mean literally *only
a little more*. We'll also use the `@DataProvider` concept in TestNG to give us some flexibility for when we create
more test data.

The weakness of the `writeOntology` test is that it is internal; it doesn't have any external data. We don't have
any artifacts of the test to examine. We should be able to read an external artifact (a file, for example) and use it
for reference and for examination.

With that, let's take a look an an ontology very similar to the ones we've been creating, stored in 
`src/test/resources/example1.owl`:

    <?xml version="1.0"?>
    <rdf:RDF xmlns="http://autumncode.com/ontologies/2015/example1.owl#"
             xml:base="http://autumncode.com/ontologies/2015/example1.owl"
             xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
             xmlns:owl="http://www.w3.org/2002/07/owl#"
             xmlns:xml="http://www.w3.org/XML/1998/namespace"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
             xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#">
        <owl:Ontology rdf:about="http://autumncode.com/ontologies/2015/example1.owl"/>
    </rdf:RDF>

This ontology is empty, consisting *only* of the ontology reference itself. (It's expressing the idea of a system of
knowledge that refers to ... no knowledge at all, *including* the assertion of the absence of knowledge.)

Our test code will use a data provider to construct a reference to an `OWLOntologyDocumentSource` (which will point
to this file) and the expected IRI from it (in this case, "http://autumncode.com/ontologies/2015/example1.owl").

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

As usual, our `OntologyHelper` code is far more simple than the test code is:

    public OWLOntology readOntology(OWLOntologyDocumentSource source)
            throws OWLOntologyCreationException {
        return m.loadOntologyFromOntologyDocument(source);
    }

## Adding a Class to an Ontology

Adding a class to an ontology is similar to adding a class to a Java package, with the main exception being that a
class in an ontology doesn't exist without a relationship to something else. Therefore, in Java we can say 
"a person is something," whereas in OWL, we have to say "a person is something that..."

An OWL class can't exist in a vacuum.

First, let's create a simple relationship: we'll define an ontology which can allow individuals to be people 
(i.e., "a person") or firemen. Here's our test code, which will ironically have to live in a vacuum itself until 
we write all of our utility methods:

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

We need to create `OntologyHelper.createClass` first, which looks like this:

    public OWLClass createClass(String iri) {
        return createClass(convertStringToIRI(iri));
    }
    public OWLClass createClass(IRI iri) {
        return df.getOWLClass(iri);
    }

We also need to define `OntologyHelper.createSubclass`. This will create a change for our ontology - a patch - that 
says that in the context of our ontology, a fireman is a type, a subclass, of person. It does not *apply* that patch;
that's the role of our `OntologyHelper.applyChange` method.

Here are those methods from `OntologyHelper`:

    public OWLAxiomChange createSubclass(OWLOntology o, OWLClass subclass, OWLClass superclass) {
        return new AddAxiom(o,df.getOWLSubClassOfAxiom(subclass, superclass));
    }

    public void applyChange(OWLAxiomChange ... axiom) {
        applyChanges(axiom);
    }

    private void applyChanges(OWLAxiomChange ... axioms) {
        m.applyChanges(Arrays.asList(axioms));
    }

After we apply the changes, we get the axioms related to subclass for our definition of what a fireman is, 
translate the axioms into the superclass name, and then check to make sure the resulting set is the right size (one)
and contains the right value (a reference to our person definition).

The resulting OWL, if you're interested, looks like this:

    <?xml version="1.0"?>
    <rdf:RDF xmlns="http://autumncode.com/ontologies/person.owl#"
         xml:base="http://autumncode.com/ontologies/person.owl"
         xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
         xmlns:owl="http://www.w3.org/2002/07/owl#"
         xmlns:xml="http://www.w3.org/XML/1998/namespace"
         xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
         xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#">
        <owl:Ontology rdf:about="http://autumncode.com/ontologies/person.owl"/>
    
        <!-- 
        ///////////////////////////////////////////////////////////////////////////////////////
        //
        // Classes
        //
        ///////////////////////////////////////////////////////////////////////////////////////
         -->

        <!-- Fireman -->
    
        <owl:Class rdf:about="http://autumncode.com/ontologies/person.owl#Fireman">
            <rdfs:subClassOf rdf:resource="http://autumncode.com/ontologies/person.owl#Person"/>
        </owl:Class>

        <!-- Person -->
    
        <owl:Class rdf:about="http://autumncode.com/ontologies/person.owl#Person"/>
    </rdf:RDF>

In Java, we've basically expressed something like this:

    public class Person { }
    public class Fireman extends Person { }

Let's take it a little farther. Imagine, if you will, SkyNet: a Terminator is both a Person and a Robot, but that 
doesn't preclude the existence of a person who is not a robot, nor does it mean that all robots are people. Our 
test method is similar, but has two superclasses for the definition of a Terminator.
 
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

The OWL this structure generates looks like this:

    <?xml version="1.0"?>
    <rdf:RDF xmlns="http://autumncode.com/ontologies/terminator.owl#"
         xml:base="http://autumncode.com/ontologies/terminator.owl"
         xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
         xmlns:owl="http://www.w3.org/2002/07/owl#"
         xmlns:xml="http://www.w3.org/XML/1998/namespace"
         xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
         xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#">
        <owl:Ontology rdf:about="http://autumncode.com/ontologies/terminator.owl"/>
    
        <!-- 
        ///////////////////////////////////////////////////////////////////////////////////////
        //
        // Classes
        //
        ///////////////////////////////////////////////////////////////////////////////////////
         -->
    
        <!-- Person -->
    
        <owl:Class rdf:about="http://autumncode.com/ontologies/terminator.owl#Person"/>
    
        <!-- Robot -->
    
        <owl:Class rdf:about="http://autumncode.com/ontologies/terminator.owl#Robot"/>
    
        <!-- Terminator -->
    
        <owl:Class rdf:about="http://autumncode.com/ontologies/terminator.owl#Terminator">
            <rdfs:subClassOf rdf:resource="http://autumncode.com/ontologies/terminator.owl#Person"/>
            <rdfs:subClassOf rdf:resource="http://autumncode.com/ontologies/terminator.owl#Robot"/>
        </owl:Class>
    </rdf:RDF>

However, Java cannot express these relationships, as it doesn't support multiple concrete superclasses. 

It's worth pointing out that the code that validates the relationships is horribly ugly, and not 
generally useful outside of the context of these tests. One normally wouldn't bother with that sort
of code.

Note that we've only expressed the idea that we can have an ontology that describes people, robots, 
and things that are both robots and people (i.e., Terminators). We could create Captain Kirk, 
the T-800, and R2-D2, all while ignoring the shrieks of rage from Trekkies, Star Wars fans, and
whoever cares about the Terminator.

Time to fix that. We still won't be able to describe any of these individuals besides their names
and what class they are, but that'll be enough for the next step.

## Adding an Individual to an Ontology

Let's start with the simplest structure possible: let's create an ontology that has a class of 
Person, and add Captain Kirk to the ontology as an instance of a Person.

Our test code is actually pretty simple, but verifying that we've done what we expected is a bit 
of a pain.

Most of the testing code builds a map of classes to individuals, using an `OWLReasoner`, which is
a class that extracts data from the ontology: we use it here to extract the direct instances of a 
type from a given class (after iterating through "all classes"). The `ListMultimap` is part of
Guava, which is a transitive dependency of OWLAPI.

Note also the "`AutoClosable` trick," which forces Java to dispose of the `OWLReasoner` when 
the block has completed execution.

    @Test
    public void addSimpleIndividual() throws Exception {
        OntologyHelper oh = new OntologyHelper();
        OWLOntology o = oh.createOntology("http://autumncode.com/ontologies/person.owl");
        OWLClass person = oh.createClass("http://autumncode.com/ontologies/person.owl#Person");
        OWLIndividual captainKirk = oh.createIndividual("http://autumncode.com/ontologies/person.owl#Kirk");
        oh.applyChange(oh.associateIndividualWithClass(o, person, captainKirk));
        
        // test that the individual is what we expect
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
    }

We need three additional methods in our `OntologyHelper` class, all of which are fairly simple (two
create the `OWLIndividual` references, and one returns an axiom that associates an individual with
a class):

    public OWLIndividual createIndividual(String iri) {
        return createIndividual(convertStringToIRI(iri));
    }
    private OWLIndividual createIndividual(IRI iri) {
        return df.getOWLNamedIndividual(iri);
    }

    public OWLAxiomChange associateIndividualWithClass(OWLOntology o,
                                                       OWLClass clazz,
                                                       OWLIndividual individual) {
        return new AddAxiom(o, df.getOWLClassAssertionAxiom(clazz, individual));
    }

In Java, we've basically modeled something like this:

    class Person {}
    void buildData() {
        Person captainKirk=new Person();
    }

Let's go a little further, and add individuals to our Terminator knowledge base, for a more complex example.

Here's the code that actually creates the ontology and the three classes (Person, Robot, Terminator) and the relationships between them (a Terminator is a Person and a Robot):

    OntologyHelper oh = new OntologyHelper();
    OWLOntology o = oh.createOntology("http://autumncode.com/ontologies/terminator.owl");
    OWLClass person = oh.createClass("http://autumncode.com/ontologies/terminator.owl#Person");
    OWLClass robot = oh.createClass("http://autumncode.com/ontologies/terminator.owl#Robot");
    OWLClass terminator = oh.createClass("http://autumncode.com/ontologies/terminator.owl#Terminator");

    oh.applyChange(oh.createSubclass(o, terminator, person),
            oh.createSubclass(o, terminator, robot));

Now we'd like to create the three individuals ("[Sarah](http://terminator.wikia.com/wiki/Sarah_Connor)," a human; 
"[Tank](http://terminator.wikia.com/wiki/HK-Tank)," a nonhumanoid robot, and 
"[T800](http://terminator.wikia.com/wiki/T-800_(The_Terminator))," a Terminator):

    OWLIndividual sarah = oh.createIndividual("http://autumncode.com/ontologies/terminator.owl#Sarah");
    OWLIndividual tank = oh.createIndividual("http://autumncode.com/ontologies/terminator.owl#Tank");
    OWLIndividual t800 = oh.createIndividual("http://autumncode.com/ontologies/terminator.owl#T800");
    oh.applyChange(oh.associateIndividualWithClass(o, person, sarah),
            oh.associateIndividualWithClass(o, robot, tank),
            oh.associateIndividualWithClass(o, terminator, t800));

The OWL that represents this ontology looks like this:

    <?xml version="1.0"?>
    <rdf:RDF xmlns="http://autumncode.com/ontologies/terminator.owl#"
         xml:base="http://autumncode.com/ontologies/terminator.owl"
         xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
         xmlns:owl="http://www.w3.org/2002/07/owl#"
         xmlns:xml="http://www.w3.org/XML/1998/namespace"
         xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
         xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#">
        <owl:Ontology rdf:about="http://autumncode.com/ontologies/terminator.owl"/>

        <!-- 
        ///////////////////////////////////////////////////////////////////////////////////////
        //
        // Classes
        //
        ///////////////////////////////////////////////////////////////////////////////////////
         -->

        <!-- Person -->

        <owl:Class rdf:about="http://autumncode.com/ontologies/terminator.owl#Person"/>

        <!-- Robot -->

        <owl:Class rdf:about="http://autumncode.com/ontologies/terminator.owl#Robot"/>

        <!-- Terminator -->

        <owl:Class rdf:about="http://autumncode.com/ontologies/terminator.owl#Terminator">
            <rdfs:subClassOf rdf:resource="http://autumncode.com/ontologies/terminator.owl#Person"/>
            <rdfs:subClassOf rdf:resource="http://autumncode.com/ontologies/terminator.owl#Robot"/>
        </owl:Class>

        <!-- 
        ///////////////////////////////////////////////////////////////////////////////////////
        //
        // Individuals
        //
        ///////////////////////////////////////////////////////////////////////////////////////
         -->

        <!-- Sarah -->

        <owl:NamedIndividual rdf:about="http://autumncode.com/ontologies/terminator.owl#Sarah">
            <rdf:type rdf:resource="http://autumncode.com/ontologies/terminator.owl#Person"/>
        </owl:NamedIndividual>
        
        <!-- T800 -->

        <owl:NamedIndividual rdf:about="http://autumncode.com/ontologies/terminator.owl#T800">
            <rdf:type rdf:resource="http://autumncode.com/ontologies/terminator.owl#Terminator"/>
        </owl:NamedIndividual>
        
        <!-- Tank -->

        <owl:NamedIndividual rdf:about="http://autumncode.com/ontologies/terminator.owl#Tank">
            <rdf:type rdf:resource="http://autumncode.com/ontologies/terminator.owl#Robot"/>
        </owl:NamedIndividual>
    </rdf:RDF>

Here's where things get interesting. We said that the T800 is a Terminator, not that it's a Robot or a Person; our test
needs to validate its participation in all three classes (Terminator, Robot, Person), even though our OWL doesn't *say*
that it's a member of all three classes. This is where the `OWLReasoner` comes in; it's able to infer the complete
class hierarchy for an individual (or, as we're using it here, it's able to find all individuals who are members of a given class.) 

This is the beginning of seeing some real power in OWL, even though we're expressing something very simple: we can describe what things are (a Terminator is a Robot and a Person), express characteristics about individuals (a T800 is a Terminator) and extract meaningful data from the ontology (i.e., that the T800 is both a Robot and a Person, while the Tank is only a Robot.)

Here's the *complete* test method, including the extraction of individuals into classes and the conversion of data into
comparable sets:

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

## Adding a Property to a Class

It's potentially useful to be able to determine class relationships and individual participation in classes, but we're
not actually describing much yet. In our Terminator example, for example, we have *one* Tank, *one* T800, *one* Sarah
Connor. Since we know that there are many T800s and many Tanks (and, depending on how much you liked [Terminator:Genisys](http://www.imdb.com/title/tt1340138/), multiple Sarahs as well) we need to have a way of differentiating individuals beyond their names.

Let's start by looking at a simpler description of people, and build a genealogy structure. We'll have three classes 
(Human, Female, and Male), and add two properties to Person (references to mother and father). Since Mother and Father are derived from Person, we can declare individuals by gender and inherit the properties referring to parentage from Human.

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
            oh.associateObjectPropertyWithClass(o, hasFather, human, male),
            oh.associateObjectPropertyWithClass(o, hasMother, human, female)
    );

We need to add `associateObjectPropertyWithClass` to `OntologyHelper`:

    /**
     * With ontology o, property in refHolder points to a refTo.
     *
     * @param o The ontology reference
     * @param property the data property reference
     * @param refHolder the container of the property
     * @param refTo the class the property points to
     * @return a patch to the ontology
     */
    public OWLAxiomChange associateObjectPropertyWithClass(OWLOntology o, 
                                                           OWLObjectProperty property, 
                                                           OWLClass refHolder, 
                                                           OWLClass refTo) {
        OWLClassExpression hasSomeRefTo=df.getOWLObjectSomeValuesFrom(property, refTo);
        OWLSubClassOfAxiom ax=df.getOWLSubClassOfAxiom(refHolder, hasSomeRefTo);
        return new AddAxiom(o, ax);
    }

We also want to add another method to the `OntologyHelper`, to manage a disjunction of classes. Our test doesn't 
use it (but our next one will): what it says is that the first class cannot be the second class. Note that a
disjunction like this is not reflexive - saying that 'a' cannot be a 'b' does not imply that 'b' cannot be an 'a';
you have to specify the disjunction both ways if you're trying to express that relationship. We'll see how that
works soon.

    /**
     * With ontology o, an object of class a cannot be simultaneously an object of class b.
     * This is not implied to be an inverse relationship; saying that a cannot be a b does not
     * mean that b cannot be an a.
     * @param o the ontology reference
     * @param a the source of the disjunction
     * @param b the object of the disjunction
     * @return a patch to the ontology
     */
    public OWLAxiomChange addDisjointClass(OWLOntology o, OWLClass a, OWLClass b) {
        OWLDisjointClassesAxiom expression=df.getOWLDisjointClassesAxiom(a, b);
        return new AddAxiom(o, expression);
    }


The test code generates the following OWL:

    <?xml version="1.0"?>
    <rdf:RDF xmlns="http://autumncode.com/ontologies/genealogy.owl#"
         xml:base="http://autumncode.com/ontologies/genealogy.owl"
         xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
         xmlns:owl="http://www.w3.org/2002/07/owl#"
         xmlns:xml="http://www.w3.org/XML/1998/namespace"
         xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
         xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#">
        <owl:Ontology rdf:about="http://autumncode.com/ontologies/genealogy.owl"/>

        <!-- 
        ///////////////////////////////////////////////////////////////////////////////////////
        //
        // Object Properties
        //
        ///////////////////////////////////////////////////////////////////////////////////////
         -->

        <!-- hasFather -->

        <owl:ObjectProperty rdf:about="http://autumncode.com/ontologies/genealogy.owl#hasFather"/>
        
        <!-- hasMother -->

        <owl:ObjectProperty rdf:about="http://autumncode.com/ontologies/genealogy.owl#hasMother"/>

        <!-- 
        ///////////////////////////////////////////////////////////////////////////////////////
        //
        // Classes
        //
        ///////////////////////////////////////////////////////////////////////////////////////
         -->

        <!-- Female -->

        <owl:Class rdf:about="http://autumncode.com/ontologies/genealogy.owl#Female">
            <rdfs:subClassOf rdf:resource="http://autumncode.com/ontologies/geneaology.owl#Human"/>
        </owl:Class>

        <!-- Male -->

        <owl:Class rdf:about="http://autumncode.com/ontologies/genealogy.owl#Male">
            <rdfs:subClassOf rdf:resource="http://autumncode.com/ontologies/geneaology.owl#Human"/>
        </owl:Class>

        <!-- Human -->

        <owl:Class rdf:about="http://autumncode.com/ontologies/geneaology.owl#Human">
            <rdfs:subClassOf>
                <owl:Restriction>
                    <owl:onProperty rdf:resource="http://autumncode.com/ontologies/genealogy.owl#hasFather"/>
                    <owl:someValuesFrom rdf:resource="http://autumncode.com/ontologies/genealogy.owl#Male"/>
                </owl:Restriction>
            </rdfs:subClassOf>
            <rdfs:subClassOf>
                <owl:Restriction>
                    <owl:onProperty rdf:resource="http://autumncode.com/ontologies/genealogy.owl#hasMother"/>
                    <owl:someValuesFrom rdf:resource="http://autumncode.com/ontologies/genealogy.owl#Female"/>
                </owl:Restriction>
            </rdfs:subClassOf>
        </owl:Class>
    </rdf:RDF>

Now let's start describing a genealogy. We're describing a few generations of a family:

* Thomas, w. Shirley: Michael and Vicki
* Barry, w. Shirley: Joseph
* Samuel, w. Mary: Andrew
* Andrew, w. Vicki: Jonathan

Here's the code to build the geneaology structure, including the exclusion of Male and Female, along with the 
actual family tree:

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

    // we'll be adding code here!
    }

The OWL this generates is pretty long, unfortunately. But here are some relevant parts from it, the Male and Female class declarations, along with Jonathan's individual reference:

    <!-- Female -->

    <owl:Class rdf:about="http://autumncode.com/ontologies/genealogy.owl#Female">
        <rdfs:subClassOf rdf:resource="http://autumncode.com/ontologies/geneaology.owl#Human"/>
        <owl:disjointWith rdf:resource="http://autumncode.com/ontologies/genealogy.owl#Male"/>
    </owl:Class>

    <!-- Male -->

    <owl:Class rdf:about="http://autumncode.com/ontologies/genealogy.owl#Male">
        <rdfs:subClassOf rdf:resource="http://autumncode.com/ontologies/geneaology.owl#Human"/>
    </owl:Class>

    <!-- jonathan -->

    <owl:NamedIndividual rdf:about="http://autumncode.com/ontologies/genealogy.owl#jonathan">
        <rdf:type rdf:resource="http://autumncode.com/ontologies/genealogy.owl#Male"/>
        <hasFather rdf:resource="http://autumncode.com/ontologies/genealogy.owl#andrew"/>
        <hasMother rdf:resource="http://autumncode.com/ontologies/genealogy.owl#vicki"/>
    </owl:NamedIndividual>

If we didn't have that disjunction of Male and Female, there would be nothing in the ontology suggesting that
a "has mother of" reference couldn't point to, say, Samuel. (Let's keep gender identity simple for the clarity's
sake.)

What happens if we actually add invalid data, though? Let's try it.

    OWLReasoner reasoner = new JFactFactory().createReasoner(o);
    try (final AutoCloseable ignored = reasoner::dispose) {
        assertTrue(reasoner.isConsistent());
    }
    oh.applyChange(oh.associateIndividualWithClass(o, female, jonathan));
    reasoner = new JFactFactory().createReasoner(o);
    try (final AutoCloseable ignored = reasoner::dispose) {
        assertFalse(reasoner.isConsistent());
    }

Here, we're checking to make sure our ontology is consistent (without error) - then we're intentionally
adding a declaration that Jonathan is female (leaving the reference to Male intact). Therefore, Jonathan is both
male and female, which our disjunction says is invalid... and when we check our reasoner again, it shows that the
ontology is inconsistent.

### Confession

Unfortunately, this is *also* where the OWLAPI starts to lose me.

The Semantic Web technologies not only provide a way to store information like our genealogy data, but query it; 
we should be able to examine Joseph and Vicki, for example, and find that they are siblings because one or both of
their parents are the same.

Even better, I *think* we can even declare rules such that a reasoner could *assert* that they were siblings
because of common parentage. Someone online even showed me how such a rule would be written in one of the OWL
formats.

However, it's not clear to me (yet) how to express such a relationship using OWLAPI. I can see a potential path
using various axioms chained together; many of the `OWLDataFactory` methods even accept arrays of arguments, 
which would seem to enable this. 

But I haven't been able to properly or simply conceptualize how all of this would work in implementation rather
than in theory, which speaks of two failures:

1. The available material has failed to make it simple enough such that it's accessible to newbies like me, and 
thus...
1. I have failed to completely grasp the subject matter enough to write authoritatively about it.

I have no problem acknowledging the second failure. 

I'm writing this tutorial to say "here's a complete reference" but instead to preserve what I have learned in 
using OWLAPI incompletely, as a data transferral protocol and as a static data store rather than as a 
reasoning engine.

If someone more experienced than I would like to collaborate on this to correct both my ignorance and the tutorial's
incompleteness, you're welcome and invited to participate.

### Back to Regular Programming

The reasoning process that shows the invalidity of our data based on the hierarchy shows something important about
OWL: it doesn't throw out our invalid data, but provides a way to tell us that our data is incorrect based on the
assertions we've made.

In other words, we said that a Human cannot be both Male and Female, and then we told it that a human was, in 
fact, both. (I'm avoiding cis gender issues, thank you; this is a simple example and isn't meant to be a treatise
on human biology or gender issues.) Because of the assertion of singular gender (and then the use of data that
violated the assertion), our ontology is no longer consistent with itself - and the reasoner was able to tell us
that.

If we had greater reasoning skills, we could derive actual family models - determine cousins, uncles, aunts, 
siblings, and more - and have the model tell us of gaps and unexpected relationships, all expressed and generated
programmatically.

However, at present most of that is beyond your author's knowledge and skill, and is beyond this tutorial's scope.

## Review

* We have created, saved, and loaded ontologies, which represents a system of knowledge
* We have created classes in an ontology, which represent ways of thinking about items.
** We have seen that classes can be present in a hierarchy, such that elements of class B are also elements of type A.
** We have seen that we can also declare classes as optionally being exclusive of one 
another. Therefore, we can say that if something is of class A, it cannot legitimately be of class B at the same time.
* We have seen how we can create individuals in an ontology.
** We have seen how we can add attributes to the individuals such that we define their classes, 
and we've seen that an individual is not limited to one class.
** We have added references to other individuals to a given individual, and also declare that the references 
must be drawn from a specific domain. (We can say that a reference, A, must be to members of class B.)
* We have seen how to use a reasoner to validate that our ontology, the class structure, and the individuals in 
it are consistent, with no contradictory information.

One thing we've not done, though, is add simple data to an individual.

## Adding a Property Value to an Individual

When we add a reference to an individual as a property of another individual ("John's mother is Andrea"), 
we are adding what OWLAPI calls an "Object Property." Another type of property is a Data Property, which
can be used in almost the exact same way.

Let's go back to the Terminator. We'll declare a simple ontology that tracks the T800, and tracks whether
it is an enemy or not.

Here's our ontology declaration as a test. After we see this, we'll take a look at the `OntologyHelper` methods
that make it work:

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

Now the `OntologyHelper` methods for creating the data property and adding data to a given individual:

    public OWLDataProperty createDataProperty(String iri) {
        return createDataProperty(convertStringToIRI(iri));
    }

    public OWLDataProperty createDataProperty(IRI iri) {
        return df.getOWLDataProperty(iri);
    }

    public OWLAxiomChange addDataToIndividual(OWLOntology o, OWLIndividual individual, 
            OWLDataProperty property, String value) {
        OWLLiteral literal = df.getOWLLiteral(value, OWL2Datatype.XSD_STRING);
        return new AddAxiom(o, df.getOWLDataPropertyAssertionAxiom(property, individual, literal));
    }

    public OWLAxiomChange addDataToIndividual(OWLOntology o, OWLIndividual individual, 
            OWLDataProperty property, boolean value) {
        OWLLiteral literal = df.getOWLLiteral(value);
        return new AddAxiom(o, df.getOWLDataPropertyAssertionAxiom(property, individual, literal));
    }

    public OWLAxiomChange addDataToIndividual(OWLOntology o, OWLIndividual individual, 
            OWLDataProperty property, int value) {
        OWLLiteral literal = df.getOWLLiteral(value);
        return new AddAxiom(o, df.getOWLDataPropertyAssertionAxiom(property, individual, literal));
    }

These are, as you might expect, almost exact analogies for the methods that add object properties to individuals.
The main difference here is that we actually deal with data, so we have overloaded argument types for the
`addDataToIndividual` method. (The `getOWLDataPropertyAssertionAxiom` method can take the actual primitive
values and render the proper types.)

So what does the generated data look like? That's simple enough:

    <?xml version="1.0"?>
    <rdf:RDF xmlns="http://autumncode.com/ontologies/terminator.owl#"
         xml:base="http://autumncode.com/ontologies/terminator.owl"
         xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
         xmlns:owl="http://www.w3.org/2002/07/owl#"
         xmlns:xml="http://www.w3.org/XML/1998/namespace"
         xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
         xmlns:terminator="http://autumncode.com/ontologies/terminator.owl#"
         xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#">
        <owl:Ontology rdf:about="http://autumncode.com/ontologies/terminator.owl"/>

        <!-- 
        ///////////////////////////////////////////////////////////////////////////////////////
        //
        // Data properties
        //
        ///////////////////////////////////////////////////////////////////////////////////////
         -->

        <!-- killsHumans -->

        <owl:DatatypeProperty rdf:about="http://autumncode.com/ontologies/terminator.owl#killsHumans"/>

        <!-- 
        ///////////////////////////////////////////////////////////////////////////////////////
        //
        // Classes
        //
        ///////////////////////////////////////////////////////////////////////////////////////
         -->

        <!-- Terminator -->

        <owl:Class rdf:about="http://autumncode.com/ontologies/terminator.owl#Terminator"/>

        <!-- 
        ///////////////////////////////////////////////////////////////////////////////////////
        //
        // Individuals
        //
        ///////////////////////////////////////////////////////////////////////////////////////
         -->

        <!-- pops -->

        <owl:NamedIndividual rdf:about="http://autumncode.com/ontologies/terminator.owl#pops">
            <rdf:type rdf:resource="http://autumncode.com/ontologies/terminator.owl#Terminator"/>
            <killsHumans rdf:datatype="http://www.w3.org/2001/XMLSchema#boolean">false</killsHumans>
        </owl:NamedIndividual>

        <!-- t800 -->

        <owl:NamedIndividual rdf:about="http://autumncode.com/ontologies/terminator.owl#t800">
            <rdf:type rdf:resource="http://autumncode.com/ontologies/terminator.owl#Terminator"/>
            <killsHumans rdf:datatype="http://www.w3.org/2001/XMLSchema#boolean">true</killsHumans>
        </owl:NamedIndividual>
    </rdf:RDF>

## Final Thoughts

What we've tried to cover is actually pretty light: "how to add data into an ontology using OWLAPI," really.
There are supposed to be a lot of features provided by OWL that we've not even begun to touch; we should,
for example, be able to infer which T800s are to be avoided in our last example (based on whether the
individual kills humans or not.)

At the very least, we should be able to write a query to determine whether a T800 is a bad guy or not.
However, that's out of the scope of this tutorial at this point; I mostly wanted to preserve what I knew
of OWLAPI for others to use.