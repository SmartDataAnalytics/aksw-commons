#!/bin/bash

virt_port=$1
virt_userName=$2
virt_passWord=$3

DIR="$( cd "$( dirname "$0" )" && pwd )"

"$DIR/virt-sparql.sh" "$virt_port" "$virt_userName" "$virt_passWord" "Select Distinct ?g { Graph ?g { ?s ?p ?o . Filter(true) } }"

