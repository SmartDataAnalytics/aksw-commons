

#### svn stor passwords in gnome keyring ####
this is a summary of: http://ubuntuforums.org/showthread.php?t=1348567
  1. in ~/.subversion/config add
```
store-passwords = yes
store-auth-creds = yes
password-stores = gnome-keyring
```
  1. in ~/.subversion/servers add
```
[global]
store-passwords = yes
store-plaintext-passwords = no
```
  1. (verify) delete ~/.subversion/auth

#### set mimetype for public documents in our paper svn ####
```
svn propset  svn:mime-type application/pdf public.pdf 
```

#### creating svn tags ####
```
svn copy http://svn.example.com/trunk http://svn.example.com/tags/mytag -m " tag message " 
```


#### hg addremove for svn ####
```
svn st | grep ! | sed 's/!//' | xargs svn delete ; svn st | grep '?' | sed 's/?//' | xargs svn add
```

#### a simple diff file viewer ####
```
grep '^+' diff.txt | sed 's/+//' >! pos.txt ; grep '^-' diff.txt | sed 's/-//' >! neg.txt ; tkdiff pos.txt neg.txt ; rm pos.txt neg.txt
```

#### repair working copy with local changes after .svn folder was deleted ####
```
mv /var/www/html/somefolder /somefolder
svn update /var/www/html/somefolder 
mv /somefolder /var/www/html/somefolder
svn add /var/www/html/somefolder --force
svn commit -m "..."
```

#### make svn ignore for several file endings and the whole repo ####
```
echo "*.aux\n*.bbl\n*.blg\n*.bst\n*.dvi\n*.idx\n*.lof\n*.log\n*.pdf\n*.toc" > ignore-these 
svn -R propset svn:ignore . -F ignore-these
```

### sherif test ###