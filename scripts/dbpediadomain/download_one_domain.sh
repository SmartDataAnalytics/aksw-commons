#!/bin/sh
RESULTDIR=results/
FILE=$RESULTDIR"instance_types_en.nt.bz2"
DOMAINURIS=$RESULTDIR"domainUris.sorted"
DATAFILEBZ=$RESULTDIR"originalData.nt.bz"
DATAFILE=$RESULTDIR"originalData.nt"
DOMAINDATAFILE=$RESULTDIR"domainData.nt"

#got to tmp dir and download the type information
if [ -f $FILE ];
then
   echo $FILE" exists already, reusing it." 
else
   mkdir $RESULTDIR
   echo "downloading http://downloads.dbpedia.org/3.6/en/instance_types_en.nt.bz2"
   wget -O $FILE http://downloads.dbpedia.org/3.6/en/instance_types_en.nt.bz2 
fi



echo "getting all subject uris for domain '"$1"'"
echo "TIPP: this can be a regex such as  Film\|Actor, but needs to be escaped properly to go into grep. "
bzcat $FILE |  grep $1 | cut -f1 -d '>' | sed 's/<//' | sort -u > $DOMAINURIS



echo "downloading domaindata from "$2" into "$DATAFILE
wget -O $DATAFILEBZ $2
bunzip2 $DATAFILEBZ


./clean.php  $DOMAINURIS $DATAFILE > $DOMAINDATAFILE

#validate
rapper -g $DOMAINDATAFILE > /dev/null

