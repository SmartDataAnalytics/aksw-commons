#!/bin/sh
mkdir data
mkdir results
mkdir instancetypes

cd instancetypes
wget http://downloads.dbpedia.org/3.6/links/instance_types_en.nt.bz2
bzcat instance_types_en.nt.bz2 |  grep 'Film\|Actor' > FilmActorTypes.nt
cat FilmActorTypes.nt | cut -f1 -d '>' | sed 's/<//' | sort -u > ../results/film_actor.uri
cd..

cd data
wget http://downloads.dbpedia.org/3.6/links/freebase_links.nt.bz2
wget http://downloads.dbpedia.org/3.6/en/images_en.nt.bz2
wget http://downloads.dbpedia.org/3.5.1/de/labels_de.nt.bz2
wget http://downloads.dbpedia.org/3.6/en/mappingbased_properties_en.nt.bz2
wget http://downloads.dbpedia.org/3.6/en/persondata_de.nt.bz2
wget http://downloads.dbpedia.org/3.6/de/short_abstracts_de.nt.bz2
bzcat *.bz2 >! alldata.nt
bunzip2 -k labels_de.nt.bz2
cd ..
./clean.php  results/film_actor.uri data/labels_de.nt | cut -f1 -d '>' | sed 's/<//' | sort -u > results/film_actor_german.uri
./clean.php  results/film_actor_german.uri data/alldata.nt >! results/data.nt
./clean.php  results/film_actor.uri data/labels_de.nt > results/labels.nt

cp results/* ../src/main/resources

#validate
rapper -g results/data.nt > /dev/null

