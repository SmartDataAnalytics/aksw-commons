

#### basic archetype (UNTESTED) ####
```
mvn archetype:create -DgroupId=org.mypackage -DartifactId=mypackage
```

#### GWT project creation (UNTESTED) ####
```
mvn archetype:generate   -DarchetypeGroupId=org.codehaus.mojo  -DarchetypeArtifactId=gwt-maven-plugin  -DarchetypeVersion=1.1   -DgroupId=org.mypackage   -DartifactId=gwt
```

#### eclipse project file ####
```
mvn eclipse:eclipse -DdownloadSources=true  -DdownloadJavadocs=true
```

#### display dependency tree ####
```
mvn  dependency:tree
```

#### upload a file to the maven aksw repository (requires user name and password in settings.xml) ####
```
mvn deploy:deploy-file -Dfile=XXX -DrepositoryId=maven.aksw.internal -Durl=http://maven.aksw.org/archiva/repository/internal -DartifactId=XXXX -DgroupId=XXXX -Dversion=XXX -Dpackaging=jar
```

#### Install to local repository + sources and javadoc ####
```
mvn install -DperformRelease=true 
```

#### Skip Tests ####
```
mvn -Dmaven.test.skip=true install
```

### clean the buildpath ###
```
mvn clean
```
try this first when something screwed up the build cycle (in my case after switching from svn to mercurial, no .class files were generated but mvn compile said "success" - mvn clean fixed it)

### execute a java class ###
```
mvn exec:java -e -o -Dexec.mainClass="org.dllearner.scripts.improveWikipedia.DBpediaClassLearnerCELOE"
```
with arguments:
```
mvn exec:java -e -o -Dexec.mainClass="org.dllearner.scripts.improveWikipedia.DBpediaClassLearnerCELOE" -Dexec.args="arg1 arg2"
```


### change the port for a jetty ###
```
mvn -Djetty.port=9999 jetty:run
```