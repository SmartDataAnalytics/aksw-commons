virt_isql="$(dirname $0)/isql"

virt_graphName=$1
virt_port=$2
virt_userName=$3
virt_passWord=$4

todoDir=`pwd`'/status_virtload/todo'
doneDir=`pwd`'/status_virtload/done'
failDir=`pwd`'/status_virtload/fail'

for fileName in `find $todoDir/*`
do
    virt_load.sh $fileName $virt_graphName $virt_port $virt_userName $virt_passWord

    [ $? -ne 0 ] && targetDir="$failDir" || targetDir="$doneDir"

    mkdir -p "$targetDir"
    mv "$fileName" "$targetDir"
done
