#!/bin/bash


#
# Executes the given command and attempts to re run it on any other exit code than 0
#
# Usage:
#    crash-recover.sh command
#


limit=60

while true; do
    startTime=`date +%s`
    $@
    exitCode="$?"
    if [ "$exitCode" -eq 0 ]; then
        echo "Normal termination"
        exit 0
    else
        echo "Crash detected with exit code $exitCode"
        endTime=`date +%s`

        delta=$((endTime-startTime))

        if [ "$delta" -lt "$limit" ]; then
            echo "Crashed after $delta seconds before reaching the $limit seconds limit) - terminating"
            exit "$exitCode"
        else
            echo "Attempting recovery..."
        fi
    fi
done

