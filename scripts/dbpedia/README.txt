In order to load a DBpedia dataset into a virtuoso store, please follow this procedure:

1.) Put virtload.sh and virtload_list.sh into the <virtuoso-root>/bin folder

2.) Create a list of URLs which should be downloaded and loaded
    (Such lists may already be provided in the svn)

2.) Download the DBpedia datasets into a todo-list directory:
wget -i <uri-list-file> -nd -P virtload_status/todo

3.) Process the todo-list by executing
./virtload_list <graphname> <port> <username> <password>