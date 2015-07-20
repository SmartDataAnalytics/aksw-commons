### make classpath ###

```
JARPATH=""; for jar in `find . -name "*.jar"`; do JARPATH=$JARPATH:$jar ; done; echo $JARPATH
```