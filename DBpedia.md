Please cross check this description with the [read-me in the SVN](http://code.google.com/p/aksw-commons/source/browse/scripts/dbpedia/README.txt) for recent changes. (Maybe we could link this wikipage into tho svn?)


### Loading ###
Set up [virtload.sh](http://code.google.com/p/aksw-commons/source/browse/scripts/virtuoso/virtload.sh) and [virtload\_list.sh](http://code.google.com/p/aksw-commons/source/browse/scripts/virtuoso/virtload_list.sh) as usual by copying these files to `<virtuoso-root>/bin`.

Then create or reuse (see next section) a file containing a list of URIs to be loaded with wget.

Load the files into a todo-list:

**Note**: Currently this must be done from `<virtuoso-root>/bin`.
```
wget -i <uri-list-file> -nd -P virtload_status/todo
```


Then process the todo-list with:
```
./virtload_list.sh <graphname> <port> <username> <password>
```


### Preconfigured lists ###
[DBpedia 3.6: Todo: add description - who used it for what purpose](http://code.google.com/p/aksw-commons/source/browse/scripts/dbpedia/)