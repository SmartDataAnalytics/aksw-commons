#!/bin/bash
newUser=$1

if [ -z "$newUser" ]; then
        echo "No username given."
        exit 1
fi

sudo useradd -d "/home/$newUser" -m "$newUser"
sudo passwd "$newUser"
