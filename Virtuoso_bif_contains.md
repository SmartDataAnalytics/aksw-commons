**There is an implementation in commons.springs.Bif Contains**

### some tests on 6.1.2 and the official endpoint ###
Findings:
  * Case-insensitive in any case
  * "" always denotes the exact string, but can be omitted, if there is no whitespace
  * if star is used " is needed
  * see also here: http://docs.openlinksw.com/virtuoso/queryingftcols.html#containspredicate

Test query was:
```
SELECT * WHERE { 
<http://dbpedia.org/resource/Febreze> rdfs:comment ?o .
FILTER (bif:contains(?o,'XXX'))
} 
```

test string:
```
"Febreze is a brand of household odor eliminator manufactured by Procter &amp; Gamble, sold in North America and Europe. First introduced in test markets in 1996, the product has been sold in the United States since June 1998, and the line has since branched out to include air fresheners (Air Effects), fabric refreshers, plug-in oils (NOTICEables), scented disks (Scentstories), and odor eliminating candles."@en
```

### WITH " ###
#### working ####
```
'"or"' ,  '"and"', but takes long time
'"house*"' , matches household
'"hous*old"' 
'"brand"' 
'"brand of household"' 
'"brand" AND "household"' 
'"brand" OR  "household"' 
```
#### NOT working, i.e. not matching or syntax error ####
```
'"brand household"' 
'"or*"' ,  '"and*"', are too short
'brand household',  produces a syntax error
```

### WITHOUT ###
#### working ####
```
'brand' 
'household AND brand'
'household OR brand'
```
### NOT working, i.e. not mathcing or syntax error ###
```
'brand household'
'brand of household'
```