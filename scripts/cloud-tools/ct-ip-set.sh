#!/bin/bash
newIp=$1

if [ -z "$newIp" ]; then
        echo "No ip given."
        exit 1
fi

