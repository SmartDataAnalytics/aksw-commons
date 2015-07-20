Download pellet and use this script in bin to start it:
```
#!/bin/bash
java -Xmx512m   -jar /opt/pellet-2.2.2/lib/pellet-cli.jar "$@"
```

To get an inferred model use:
```
pellet extract file.owl
```

and chain it with rapper
```
pellet extract file.owl | rapper -g - 
```

some options (see pellet help extract) which have to be in quotes and space separated (copy paste from here )

--statements, -s (Space separated list surrounded by quotes)
> Statements to extract. The option accepts all axioms of the OWL functional
> syntax plus some additional ones. Valid arguments are:  Example: "DirectSubClassOf
> DirectSubPropertyOf" (Default: DefaultStatements)
```
pellet extract -s "DefaultStatements AllClass AllIndividual AllProperty AllStatements AllStatementsIncludingJena ClassAssertion ComplementOf DataPropertyAssertion DifferentIndividuals DirectClassAssertion DirectSubClassOf DirectSubPropertyOf DisjointClasses DisjointProperties EquivalentClasses EquivalentProperties InverseProperties ObjectPropertyAssertion PropertyAssertion SameIndividual SubClassOf SubPropertyOf" file.owl
```