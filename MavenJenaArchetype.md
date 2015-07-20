# About #

Maven is a dependency management tool we use because nobody wants to bother around with copied .jar files in the repos.
This archetype instantly creates a project with a small "Hello World" application in it.
The "Hello World" application retrieves all(!!1!) its data from an internal triple store (jena) via SPARQL.

Just follow these steps.

## 1. Prepare ##
  * Install maven (ubuntu: sudo apt-get install maven)
  * With eclipse: http://www.eclipse.org/m2e/ Maven Eclipse plugin

## 2. Create the project ##


Just execute from a folder in which the project should be created the command:

---

` mvn archetype:generate  -DgroupId=dummy.invalid -DartifactId=helloworld -DarchetypeRepository="http://maven.aksw.org/archiva/repository/snapshots/" -DarchetypeGroupId=org.aksw -DarchetypeArtifactId=jena-archetype -DarchetypeVersion=0.9-SNAPSHOT  -DinteractiveMode=false `

---

You might want to change `dummy.invalid` and `helloworld` into something more fitting.

## 3. Eclipse integration ##


To have a full set up Eclipse IDE, change directory into the **project folder** and execute on the command line:

---

`mvn eclipse:eclipse -DdownloadSources -DdownloadJavadoc`

---

And import everything into Eclipse, using File->Import->General->Existing Project into Workspace


Have fun!