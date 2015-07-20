simple starting script (paths need to be adapted):

```
#!/bin/bash
wd=`pwd`
iniFile="$wd/virtuoso.ini"
sudo ../../bin/virtuoso-t -f -c "$iniFile"
```

starting with automatic crash recovery (restarts Virtuoso automatically in case of a crash; paths need to be adapted):

```
#!/bin/bash
wd=`pwd`
iniFile="$wd/virtuoso.ini"
ulimit -c unlimited
#sysctl vm.swappiness=10

limit=60

while true; do
    startTime=`date +%s`

    ../../bin/virtuoso-t -f -c "$iniFile"
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
```