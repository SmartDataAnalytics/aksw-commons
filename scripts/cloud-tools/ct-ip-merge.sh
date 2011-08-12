#!/bin/bash


base=$1
der=$2
delim=$3

declare -a baseArr derArr

baseArr=(`echo $base | tr "$delim" ' '`)
derArr=(`echo $der | tr "$delim" ' '`)



bal=${#baseArr[@]}
dal=${#derArr[@]}
offset=$((bal - dal))

for i in `seq 1 $dal`; do
	baseArr[$offset + $i - 1]=${derArr[$i - 1]}
done



result=${baseArr[0]}
for i in `seq 1 "$(($bal - 1))"`; do
	result="$result"$delim${baseArr[$i]}
done


echo "$result"

#echo "${baseArr[@]}"

