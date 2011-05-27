#!/bin/bash
#Checks out aksw-commons under opt as the current user and group

# TODO Check if directory already exists and is non empty
# In that case only update the repo and recreate the symlinks

dir="/opt/aksw-commons/"
sudo mkdir -p $dir
chown $USER:$USER $dir

hg clone https://aksw-commons.googlecode.com/hg/ /opt/aksw-commons

ln -s /opt/aksw-commons/scripts/cloud-tools ~/.

chmod +x ~/cloud-tools/*.sh
