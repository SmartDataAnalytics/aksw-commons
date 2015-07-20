#### Command for In / Outdegree on NTriple files ####
outdegree of subjects
```
cat datafile.nt | cut -f1 -d '>' | sed 's/<//;s/>//' | awk '{count[$1]++}END{for(j in count) print "<" j ">" "\t"count [j]}' > outdegree_subjects.txt 
```
usage count of predicates
```
cat datafile.nt | cut -f2 -d '>' | sed 's/<//;s/>//' | awk '{count[$1]++}END{for(j in count) print "<" j ">" "\t"count[j]}' > Predicate_Degree.data 
```
indegree of objects
```
cat datafile.nt | grep -v '"' | cut -f3 -d '>' | sed 's/<//;s/>//' | awk '{count[$1]++}END{for(j in count) print "<" j ">" "\t"count[j]}' > Objects_Indegree.data 
```

#### Students Work ####
##### TF-IDF #####
http://kenai.com/projects/statistiktool
#### HITS ####
http://kenai.com/projects/rdf-statistik-tools/pages/Home


#### Class Owl Extraction ####

Command for extracting owl:Class from owl file

```
cat ontology.owl | grep 'owl:Class' | cut -f2 -d '>' | sed 's/owl:class rdf about=//;'
```

#### Create Table along with inserting DATA ####
```
CREATE TABLE DBpediaStatisticInfo AS (sparql DEFINE output:valmode "LONG"  select distinct ?s as ?id, 0 as type, 0 as ? Ingedree, 0 as ?Outdegree FROM <http://dbpedia.org> 
where {{?x ?s ?y.} union {?s ?p ?o.} union {?m ?n ?s.}}
) WITH DATA
```

#### Joining helper Tables ####
```
select id,Label,Type,Indegree,Outdegree,Degree from DBPedia_Label  
left join DBPedia_SubjectOutDegree on 
DBPedia_SubjectOutDegree.Subject=id_to_iri(DBPedia_Label.id)
left join DBPedia_ObjectInDegree on DBPedia_ObjectInDegree.Object=id_to_iri(DBPedia_Label.id)
left join DBPedia_PredicateDegree on DBPedia_PredicateDegree.Predicate=id_to_iri(DBPedia_Label.id)

```


#### Command for loading a txt file to relational table of virtuoso ####
```
csv_load(file_open('filename'),0,null,concat('DB.DBA','tablename'),2,'delimiter');
```

#### Reversing N-Triples ####
usefull for owl:sameAs
```
awk '{print $3 " " $2 " " $1 " ."}'
```