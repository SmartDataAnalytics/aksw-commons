

## Notes & Open Issues ##
  * The order of the arguments of | (pipe) seems important, i.e. [<sup>|]] is different from [</sup>]|] SH: How does [^|\]] work?
  * kurzum: Maybe it's possible to learn regex with DL-Learner?

### sed ###
  * -r seems to activate advanced regex necessary for matching |

## Copy & Paste Section below ##
### General ###
#### Find URLs ####
```
 php: $pattern = "/\b(?:(?:https?|ftp):\/\/|www\.)[-a-z0-9+&@#\/%?=~_|!:,.;]*[-a-z0-9+&@#\/%=~_|]/i";
```


#### remove Wikilinks with a pipe | in the middle ####
```
Example: [[Foo|Bar]] => Foo
sed -r 's/\[\[([^]|]*)(\|[^]]*)?\]\]/\1/g'
php: preg_replace( '#\\[\\[([^|\\]]*\\|)?(.*?)\\]\\]#s', '$2', $text );
java: Pattern pattern = Pattern.compile( "\\[\\[([^\\|\\]]*)\\|([^\\]]*)\\]\\]" );
```

#### Match single quoted strings that may contain single quotes escaped with backslash ####
```
Example: 'abc\'def' => abc\'def
java: '((\\'|[^'])*)'
```

#### Replace Whitespaces in Virtuoso console output ####
```
sed "s/^ *//;s/ *$//;s/ \{1,\}/\t/g"
s/^ *//;  Whitespaces at beginning
s/ *$//;  Whitespaces at the end
s/ \{1,\}/\t/ Whites in the middle
g  
```
### DBpedia ###
#### get a property list from ntriples ####
` cut -f2 -d " " infobox_en.nt | grep http://dbpedia.org/property/ | sed 's/<http:[/][/]dbpedia.org[/]property[/]//;s/>//' `
### String list manipulation ###
We assume that we have a line by line list of strings, i.e. words or something
#### print the length of each string ####
```
identify length
awk '{ print length(), $0 | "sort -n" }'  /usr/share/dict/words
```


### Parse out all links in a document and retrieve RDF data (if available) ###
```
#!/bin/bash

shopt -s nocasematch

filename=$1
contentType="application/rdf+xml"
accept="accept: $contentType"
grep "$filename" -oe "http://[^ \"']*" | while read uri; do
    header=`curl -ILH "$accept" "$uri"`

    #echo "header = $header"

    if [[ "$header" =~ "$contentType" ]]; then
        echo "Downloading $uri"
        wget --header="'$accept'" "$uri"
    else
        echo "Skipping $uri"
    fi

    sleep 3
done
```


### Apache logs ###
Here is a script which parses out SPARQL queries from the apache access logs.
Requires PHP for URL-decoding.

Warning: The following replacements are being performed:
  * Carriage returns (\m) are removed.
  * Tabs (\t) are replaced with 4 whitespaces
  * Newlines (\n) are replaced with tab. (Therefore each query goes on a single line)
```
#!/bin/bash

dir="."

catCmd()
{
    unzip_source=$1
    unzip_extension=${unzip_source##*.}
    unzip_target=${unzip_source%.*}

    if [ $unzip_extension = "bz2" ]; then
        echo "bzip2 -dk $unzip_source"
    elif [ $unzip_extension = "gz" ]; then
        echo "gzip -cd $unzip_source"
    else
        echo "cat $unzip_source"
    fi
}

urlDecode()
{
    while read url; do
	val=`php -r "echo urldecode('$url');" | tr -d '\015' | sed 's/\t/    /' | tr '\n' '\t'`
	echo "$val"
    done
}

for file in `ls $dir/access*`
do
    `catCmd $file` | grep "/sparql/.*query=" | sed -r 's/.*query=([^& ]+)( |&).*/\1/' | urlDecode
done
```