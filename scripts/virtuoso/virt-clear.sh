#!/bin/bash

# Script for clearing a graph in virtuoso


#virt_isql="$(dirname $0)/isql"
virt_isql='isql-vt'

graphName=$1
virt_port=$2
virt_userName=$3
virt_passWord=$4


arg="EXEC=Sparql Clear Graph <$graphName>"
echo "$arg"
$virt_isql "$virt_port" "$virt_userName" "$virt_passWord" "$arg"


