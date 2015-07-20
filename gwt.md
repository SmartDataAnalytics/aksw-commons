Checkout the working aksw prototype
```
svn checkout https://aksw-commons.googlecode.com/svn/trunk/gwt gwt
```

Adjust 3 files:
  * pom.xml
  * src/main/webapp/WEB-INF/web.xml
  * src/main/java/org/aksw/myapp/gwt/Application.gwt.xml

Remove all .svn folders:
```
find . -name ".svn" | xargs rm -r 
```

start coding