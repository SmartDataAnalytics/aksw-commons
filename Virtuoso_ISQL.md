

#### simple loading ####
make sure the directory is added in dirsAllowed:
http://docs.openlinksw.com/virtuoso/dbadm.html#fp_acliniallowed
  * turtle or n-triples
```
ttlp (file_to_string_output ('/opt/virtuoso-opensource-6.1.0/file.nt'), '', 'http://graph.org'); 
```

  * turtle or n-triples for big files
```
ttlp_mt (file_to_string_output ('/opt/virtuoso-opensource-6.1.0/file.nt'), '', 'http://graph.org'); 
```

  * RDF/XML
```
DB.DBA.RDF_LOAD_RDFXML (file_to_string_output('/opt/virtuoso-opensource-6.1.0/file.rdf'), 'no', 'http://graph.org'); 
```

#### delete a graph ####
```
SPARQL DROP GRAPH <http://localhost/OntoWiki/Config/>
SPARQL CLEAR GRAPH <http://linkedgeodata.org>
```

#### set dba password on startup ####
```
virtuoso-t -f +pwddba dba
```

#### Script for conveniently loading RDF files into virtuoso ####
  * Setup: Place the following code into a file named 'virtload.sh' located in same folder as where isql is located; alternatively adjust virt\_isql variable.

  * Usage:
```
./virtload.sh <file> <graph> <port> <user> <password>
./virtload.sh myfile.n3.bz2 http://test.org 1111 dba dba
```

  * Features:
    * Automatically decompresses zipped files.
    * Automatically splits large RDF files into chunks (after converting to n-triples format), and performs inserts on a per chunk basis.
    * No potential clashes between multiple parallel executions of the script since 'mktemp' is used for storing temporary data.
    * Automatically splits large files to /tmp, and copies small files to /tmp; reading files from this directory is allowed by virtuoso by default.

  * Missing Features:
    * Explicitly create graphs in virtuoso (virtuoso does not list non-explicely created graphs under Conductor->RDF->Graphs. Therefore, Ontowiki also doesn't recognize such graphs.

Script is available in the [repo](http://code.google.com/p/aksw-commons/source/browse/scripts/virtuoso/virtload.sh).


#### Dump a graph as ntriples ####
This script is an adaption from the dump script at http://ods.openlinksw.com/wiki/main/Main/VirtDumpLoadRdfGraphs for ntriples rather than turtle.

  * This script will chunk the dump into separate files according to the 'file\_length\_limit' parameter. If the parameter is omitted or negative, only a single file is written.
  * The file name extension '.nt' is autmatically appended to the target filename.

Example usage:
dump\_graph\_nt('http://linkedgeodata.org', '/tmp/lgddump');

```
drop procedure dump_graph_nt;
create procedure dump_graph_nt(in srcgraph varchar, in out_file varchar, in file_length_limit integer := -1)
{
  declare file_name varchar;
  declare env, ses any;
  declare ses_len, max_ses_len, file_len, file_idx integer;
  set isolation = 'uncommitted';
  max_ses_len := 10000000;
  file_len := 0;
  file_idx := 1;
  file_name := sprintf ('%s-%06d.nt', out_file, file_idx);
  string_to_file (file_name || '.graph', srcgraph, -2);
  env := vector (0, 0, 0);
  ses := string_output ();
  for (select * from (sparql define input:storage "" select ?s ?p ?o { graph `iri(?:srcgraph)` { ?s ?p ?o } } ) as sub option (loop)) do
    {
      http_nt_triple (env, "s", "p", "o", ses);
      ses_len := length (ses);
      if (ses_len > max_ses_len)
        {
          file_len := file_len + ses_len;
          if (file_length_limit >= 0 and file_len > file_length_limit)
            {
              string_to_file (file_name, ses, -1);
              file_len := 0;
              file_idx := file_idx + 1;
              file_name := sprintf ('%s-%06d.nt', out_file, file_idx);
              env := vector (0, 0, 0);
            }
          else
            string_to_file (file_name, ses, -1);
          ses := string_output ();
        }
    }
  if (length (ses))
    {
      string_to_file (file_name, ses, -1);
    }
};

```


#### Dump a query as ntriples ####
This is an adaption of the script above in order to dump an arbirary sparql select query result as n-triples.

Note: Only the first three variables of the projection of the select statement will be dumped.

A next step would be to extend it with construct queries.
It is not fully tested yet, so please verify your results.

Usage:
> Copy and paste the method into an isql shell.

Example usage:
> `dump_query_nt('Select ?s ?p ?o { ?s a ?c . ?s ?p ?o . }', '/tmp/result');`

Script is available in the [repo](http://code.google.com/p/aksw-commons/source/browse/scripts/virtuoso/dump_query_nt.vpl).

#### Add page rank to resources ####

Sometimes, especially when working with large result set, you want your results ordered by importance. Virtuoso makes it easy for you to calculate the page rank for the triples.

  * Step 1.
add faceted browser vad: Either use the web interface (in conductor: System Admin -> Packages, select fct) or command line: vad\_install ('fct\_dav.vad', 0);

  * Step 2.
run s\_rank() in isql. This takes some time.

  * Step 3.
Query, sort, whatever in your queries:

SELECT ?s (<LONG::IRI\_RANK> (?s) AS ?rank) WHERE { ?s ?p ?o } ORDER BY DESC ?rank
