#!/bin/bash
newName=$1
oldName=`cat /etc/hostname`

if [ -z "$newName" ]; then
        echo "New hostname is empty"
        exit 1
fi

if [ -z "$oldName" ]; then
        echo "Old hostname is empty - actually this should not happen"
        exit 1
fi

echo "$newName" > /etc/hostname
cat /etc/hosts | sed -r "s/(\s)$oldName(\s|$)/\1$newName\2/g" > /etc/hosts

service hostname restart
