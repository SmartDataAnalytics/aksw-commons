#!/bin/bash

# Script for loading an rdf file into a virtuoso store using
# virtuoso's isql
# Usage: sourceFile graphName port userName passWord
# e.g. <cmd> myfile.n3.bzip2 http://mygraph.org 1115 dba dba

#parameters
unzip_source=$1
virt_graphName=$2
virt_port=$3
virt_userName=$4
virt_passWord=$5

#script variables
size_of_splits=50000
virt_isql="$(dirname $0)/isql"
#virt_isql="isql-vt"
unzip_extension=${unzip_source##*.}
unzip_target=${unzip_source%.*}

# Phase 1: Unzip
#echo "Target: $unzip_target, Extension: $unzip_extension"

if [ $unzip_extension = "bz2" ]; then
	bzip2 -dk $unzip_source
elif [ $unzip_extension = "gz" ]; then
	gzip -d $unzip_source
elif [ $unzip_extension = "zip" ]; then
	unzip $unzip_source
else
	unzip_target=$unzip_source
fi


# Phase 2: Convert to n-triple
# FIXME Skip this step if the source file is already in n-triples format


rapper_source=$unzip_target
rapper_extension=${rapper_source##*.}
rapper_target="${rapper_source%.*}.nt"

if [ $rapper_extension != "nt" ]; then
    rapper_target=`mktemp`
    rapper_target="$rapper_target.nt"
    echo "Converting to n-triples. File is $rapper_target"
    rapper $rapper_source -i guess -o ntriples >> $rapper_target
fi

#echo "Unzip target= $unzip_target"
triple_count=$(stat -c%s "$rapper_target")

echo "Size = $triple_count"



if [ $triple_count -gt 5000000 ]; then
	echo "File is large, i.e. over 5 million triples ($triple_count)"
	# Phase 3: Split
	load_progress=0
	split_source=$rapper_target
	split_dir=`mktemp -d`
    echo "Performing split on file $split_source into $split_dir"
	split -a 10 -l  $size_of_splits $split_source "${split_dir}/file"

	# Phase 4: Load
	
	for file in `ls $split_dir`
	do
		load_target="$split_dir/$file"
       	load_query="EXEC=TTLP_MT(file_to_string_output('$load_target'), '', '$virt_graphName', 255);"
       	echo "loading $split_dir/$file into $virt_graphName"
	    $virt_isql "$virt_port" "$virt_userName" "$virt_passWord" "$load_query"
	    load_progress=$(($load_progress+$size_of_splits))
        percentage=$( echo "$load_progress * 100 / $triple_count" | bc -l )
        echo $percentage"% done"
#		echo "$virt_isql" "$virt_port" "$virt_userName" "$virt_passWord" "$load_query"
	done;
	# clean up split tmp dir
	rm -r $split_dir
	echo "done"
else
	echo "File is small. Loading directly."
	load_source=$rapper_target
	load_target=`mktemp`

	# NOTE By default virtuoso restricts access to files to only explicitely
	# allowed directories. By default /tmp is allowed, therefore we copy the
	# file there.
	cp $load_source $load_target

	load_query="EXEC=TTLP_MT(file_to_string_output('$load_target'), '', '$virt_graphName', 255)"


	echo "$virt_isql $virt_port $virt_userName $virt_passWord $load_query"


	$virt_isql "$virt_port" "$virt_userName" "$virt_passWord" "$load_query"
fi
