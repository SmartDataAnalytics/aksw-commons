
```
ENDPOINT=http://dbpedia.org/sparql
curl $ENDPOINT --data-urlencode  default-graph="http://dbepdia.org" --data-urlencode query="select * {?s ?p ?o} limit 10"
curl $ENDPOINT --data-urlencode  default-graph="http://dbepdia.org" --data-urlencode query@file.sparql
```
