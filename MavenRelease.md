# Introduction #

Add your content here.


## General ##
Adding the source code management (scm) system to perform a release against to the pom:
```
<project>
   <scm>
        <connection>scm:hg:https://aksw-commons.googlecode.com/hg/</connection>
        <!-- developerConnection></developerConnection -->
        <!-- url>https://my-project.googlecode.com/svn</url -->
    </scm>

...
</project>
```

## Make a big Jar ##
```
mvn assembly:single -DdescriptorId=jar-with-dependencies
```

## make a release ##
```
mvn clean -Prelease package
```
== deploy sources and javadoc snapshot
```
mvn clean javadoc:jar source:jar install
mvn clean javadoc:jar source:jar deploy
```

## NLP2RDF ##

After release:prepare has been called

```
mvn -e  release:perform -DconnectionUrl=scm:hg:https://kurzum@code.google.com/p/nlp2rdf/ -Dtag=nlp2rdf-parent-1.0 
```

Turn off Sources and JavaDoc, sometimes that helps
http://maven.apache.org/plugins/maven-release-plugin/perform-mojo.html#useReleaseProfile

```
-DuseReleaseProfile=false
```