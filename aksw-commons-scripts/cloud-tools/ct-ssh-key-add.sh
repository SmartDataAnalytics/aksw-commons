#!/bin/bash

key=$1

if [ -z "$key" ]; then
        echo "No ssh-key given."
        exit 1
fi

mkdir -p ~/.ssh

# TODO Test if the file already contains the line
echo "$key" >> ~/.ssh/authorized_keys
