#!/bin/bash

oldName=$1
newName=$2

if [ -z "$oldName" ]; then
        echo "Old name not given."
        exit 1
fi

if [ -z "$newName" ]; then
        echo "New name not given."
        exit 1
fi

oldNameXml="/tmp/$oldName.xml"
newNameXml="/tmp/$newName.xml"

virsh dumpxml "$oldName" > "$oldNameXml"
cat "$oldNameXml" | sed  -r 's\<name>[^<]*</name>\<name>'"$newName"'</name>\g' > "$newNameXml"
virsh undefine "$oldName"
virsh define "$newNameXml"
