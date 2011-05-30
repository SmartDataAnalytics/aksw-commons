#!/bin/bash
newName=$1
oldName=`cat /etc/hostname`

if [[ "$USER" != "root" ]]; then
        echo "Must be run as root"
        exit 1
fi

if [ -z "$newName" ]; then
        echo "New hostname is empty"
        exit 1
fi

if [ -z "$oldName" ]; then
        echo "Old hostname is empty - actually this should not happen"
        exit 1
fi

hostname "$newName"

content=`cat /etc/hosts | sed -r "s/(\s)$oldName(\s|$)/\1$newName\2/g"`
echo "$content" > /etc/hosts
echo "$newName" > /etc/hostname

service hostname restart
