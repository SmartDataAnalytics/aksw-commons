The code in this project is used by several open-source AKSW projects (http://aksw.org) such as:
LinkedGeoData, NLP2RDF DL-Learner, ORE
An overview of project by AKSW is given here:
http://aksw.org/Projects

How to use the Wiki:
(please mail to Sebastian Hellmann if you would like to join)
  1. enter anything you find useful to the wiki page. It should replace your personal notepad
  1. Check out the wiki somewhere on your Ubuntu laptop (AKSW likes Ubuntu)
```
hg clone --insecure https://wiki.aksw-commons.googlecode.com/hg/ aksw-commons-wiki
```
  1. create a script in your bin folder containing:
```
AKSWWIKIPATH=/path/to/aksw-commons-wiki
grep -Rhi $1 $AKSWWIKIPATH  -C3 | grep -v '{{{\|}}}' 
```

Planned content of these Utils:
  * Jena2OWLApi converter
  * lots of scripts
  * sparql wrappers