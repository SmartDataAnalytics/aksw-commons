#!/bin/bash

oldName=$1
newName=$2

oldNameXml="/tmp/$oldName.xml"
newNameXml="/tmp/$newName.xml"

virsh dumpxml "$oldName" > "$oldNameXml"
cat "$oldNameXml" | sed  -r 's\<name>[^<]*</name>\<name>'"$newName"'</name>\g' > "$newNameXml"
virsh undefine "$oldName"
virsh define "$newNameXml"
