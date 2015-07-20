#### Get all ranges for each property of DBpedia ontology ####
```
#!/bin/sh

#Download owl
wget http://downloads.dbpedia.org/3.6/dbpedia_3.6.owl

#get all ranges:
rapper -g dbpedia_3.6.owl| grep '#range' | sed "s|\s*<http://www.w3.org/2000/01/rdf-schema#range>\s*|\t|g"  | sed "s|\s*\.$||g" | sed 's/<//g;s/>//g'  |  sort -t $'\t'  -k2  > allRanges.csv

#materialize inferenced model with pellet cli http://clarkparsia.com/pellet/faq/extract-all-inferences/
pellet extract -s "DefaultStatements AllClass AllIndividual AllProperty AllStatements AllStatementsIncludingJena ClassAssertion ComplementOf DataPropertyAssertion DifferentIndividuals DirectClassAssertion DirectSubClassOf DirectSubPropertyOf DisjointClasses DisjointProperties EquivalentClasses EquivalentProperties InverseProperties ObjectPropertyAssertion PropertyAssertion SameIndividual SubClassOf SubPropertyOf" dbpedia_3.6.owl > dbpedia_3.6_infered.owl

#get all superclasses of class:
rapper -g dbpedia_3.6_infered.owl | grep 'subClass' | grep -v 'urn:x-hp' | sed "s|\s*<http://www.w3.org/2000/01/rdf-schema#subClassOf>\s*|\t|g"  | sed "s|\s*\.$||g" | sed 's/<//g;s/>//g'   | sort -t $'\t'  -k1  > allClasses.csv

#merge all properties with all classes
join allRanges.csv allClasses.csv -1 2 -2 1 | cut -f2,3 -d ' ' > allRangesInferred.csv
```