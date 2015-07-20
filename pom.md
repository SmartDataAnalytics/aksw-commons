#### Our Archivas ####
```
<repository>
     <id>maven.aksw.internal/</id>
     <name>University Leipzig, AKSW Maven2 Repository</name>
     <url>http://db0.aksw.org:8081/archiva/repository/internal</url>
</repository>
```
```
<repository>
   <id>maven.aksw.snapshots/</id>
   <name>University Leipzig, AKSW Maven2 Repository</name>
   <url>http://db0.aksw.org:8081/archiva/repository/snapshots</url>
</repository>
```

#### Exclude jars from a dependency ####
```
<dependency>
    <groupId>com.hp.hpl.jena</groupId>
    <artifactId>jena</artifactId>
    <version>2.6.2</version>
    <exclusions>
	    <exclusion>
	    <artifactId>slf4j-api</artifactId>
	    <groupId>org.slf4j</groupId>
	    </exclusion>
	    </exclusions>
</dependency>
```
#### Ant Plugin ####
Copies the web.xml into the war folder.
```
<plugin>
                <artifactId>maven-antrun-plugin</artifactId>
                <executions>
                    <execution>
                        <phase>validate</phase>
                        <goals>
                            <goal>run</goal>
                        </goals>
                        <configuration>
                            <tasks>
                                <mkdir dir="./war/WEB-INF"/>
                                <copy overwrite="true" todir="./war/WEB-INF/">
                                    <fileset dir="./src/main/webapp/WEB-INF">
                                        <include name="web.xml"/>
                                    </fileset>
                                </copy>
                            </tasks>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
```