#!/bin/bash
rm release.properties
find . -name *.releaseBackup | xargs -I@ rm @
