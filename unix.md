Things in here are especially written for the Z-Shell as we at AKSW use it.

Sed one liners: http://sed.sourceforge.net/sed1line.txt

#### cmdfu find unix commands ####
the site: http://www.commandlinefu.com
Search the site with regex from the shell (add):
```
cmdfu () {curl "http://www.commandlinefu.com/commands/matching/$@/$(echo -n $@ | openssl base64)/plaintext"}
```
Search:
```
cmdfu 'sort.*random' | less
```

#### mass rename files or folders with sed ####
```
ls | sed -r "p;s|pattern|replacement|g" | xargs -n2 mv
```

"p" means "print" so that the original name is printed first, then xargs -n2 uses two parameters each.
If the file is shared via svn or git use "xargs -n2 svn mv" or "xargs -n2 git mv" instead.
Instead of ls you can use "find -iname README" or something obviously.

#### get a percentage of random lines ####
```
#!/bin/bash
FILE=$1;
PER=$2;
NUM=$(cat $FILE | wc -l)
NUM=$(($NUM * $PER)) 
sort -r $FILE | head -$NUM
```

#### count filesize, sort and display in a nice way ####
```
du -s * | sort -n | cut -f2 | xargs du -sh
```

#### rsync home folder to backup drive ####
```
rsync -av --progress --delete $HOME /media/backupDrive
```

#### sum up a column ####
```
awk '{total = total + $1}END{print total}'
```

#### Remove duplicate entries in a file without sorting. ####
```
awk '!x[$0]++' <file>
```

#### trim (remove all leading and tailing whitespace) ####
```
sed "s|^\s*||" | sed "s|\s*$||"
```

#### analyze Silk matching for duplicates (expects matching to be in the file links\_accepted.nt) ####
```
#! /bin/sh
if [ -f links_accepted.nt ]; then 
	:
else
	echo "File links_accepted.nt does not exist. Aborting." 1>&2
	exit 1
fi 


if [ -d tmp ]; then 
	echo "Directory tmp already exists. Aborting." 1>&2
	exit 1
fi 

mkdir tmp
# convert n triples file "x(space)<http://www.w3.org/2002/07/owl#sameAs>(space)y ." into two row csv "x(tab)y"
sed "s|\s*<http://www.w3.org/2002/07/owl#sameAs>\s*|\t|g" links_accepted.nt | sed "s|\s*\.$||g" > tmp/tmp.csv
cut -f1 tmp/tmp.csv | sort > tmp/row1.txt
cut -f2 tmp/tmp.csv | sort > tmp/row2.txt
# list of duplicates with occurrence, ordered by occurrence, descending
uniq -c -d tmp/row1.txt | sort -n -k 1 -r > duplicates1.txt
uniq -c -d tmp/row2.txt | sort -n -k 1 -r > duplicates2.txt

# table of occurrence distribution, for the screen
echo "occurrences in data source 1"
uniq -c tmp/row1.txt | sort -n -k 1 | trim | cut -f1 -d " " | uniq -c | tee tmp/occurrence1.txt
echo "occurrences in data source 2"
uniq -c tmp/row2.txt | sort -n -k 1 | trim | cut -f1 -d " " | uniq -c | tee tmp/occurrence2.txt

# table of occurrence distribution, in latex table format, saved to occurrence1.tex and occurrence2.tex
cat tmp/occurrence1.txt | trim | sed "s| |\t\\&|" | sed "s|$|\\\\\\\\|" > occurrence1.tex
cat tmp/occurrence2.txt | trim | sed "s| |\t\\&|" | sed "s|$|\\\\\\\\|" > occurrence2.tex

rm -r -f tmp
```

#### match with Silk on the server ####
```
ssh root@lgd.aksw.org
screen
java -DconfigFile="filename.xml" -Dthreads=2 -jar silk.jar 2>&1 | tee  silk.out
```
hit CTRL+a d (detach screen)
```
tail -f silk.out
```

#### Read a variable from a property file ####
```
OUTPUTDIR=`sed '/^\#/d' $EXTR_DUMP/config.properties | grep 'outputDir'  | tail -n 1 | cut -d "=" -f2- | sed 's/^[[:space:]]*//;s/[[:space:]]*$//'`
```

#### Top 20 processes using the most memory ####
```
ps -eo pmem,pcpu,rss,vsize,args | sort -k 1 -r | head -20
```

#### looping over lines in a file ####
```
while read line; do
    echo "$line"
done < "$filename"
```

#### memory processes sorted by mem usage ####
```
ps aux | sort -nrk 4 | head
```

#### rapper input from pipe ####
```
cat some.rdf | rapper -I - - file
#note the spaces ( -I  -  -  file )
```