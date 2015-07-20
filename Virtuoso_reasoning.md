### Put in front of query ###
#### owl:sameAs ####
```
DEFINE input:same-as "yes"
```

#### use data from a graph as T-Box ####
prepare once:
```
rdfs_rule_set ('myidentifier', 'http://dbpedia.org');
myidentifier might be the graph+inference or anything you want
```
```
DEFINE input:inference 'myidentifier'
```