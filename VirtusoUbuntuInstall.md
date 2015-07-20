**steps necessary for including php and iODBC are marked optional**

Requirements
```
apt-get install autoconf automake libtool flex bison gperf gawk m4 make openssl libssl-dev libreadline5-dev
```

_optional: download and make iODBC_
```
sudo su
cd /tmp
wget "http://www.iodbc.org/downloads/iODBC/libiodbc-3.52.7.tar.gz"
tar xfvz libiodbc-3.52.7.tar.gz
cd //tmp/libiodbc-3.52.7/
./configure --prefix=/tmp/iODBC
make 
make install
```

_optional download and make php_
```
sudo su
cd /tmp
wget "http://de2.php.net/get/php-5.3.3.tar.gz/from/this/mirror"
tar xfvz php-5.3.3.tar.gz
cd /tmp/php-5.3.3/
./configure  --prefix=/tmp/php5 --enable-maintainer-zts --with-tsrm-pthreads --enable-embed=shared --disable-static --with-config-file-path=. --disable-cgi --disable-cli --disable-ipv6 --disable-pdo --without-mysql --without-pear --with-zlib --with-iodbc=/tmp/iODBC
make 
make install
```


Download and extract
```
cd /tmp
wget "http://downloads.sourceforge.net/project/virtuoso/virtuoso/6.1.2/virtuoso-opensource-6.1.2.tar.gz"
tar xvfz virtuoso-opensource-6.1.2.tar.gz
```

Choose one:

  * Linux 32-bit
```
    CFLAGS="-O2"
    export CFLAGS
```
  * Linux 64-bit
```
    CFLAGS="-O2 -m64"
    export CFLAGS
```


Configure and compile
```
cd /tmp/virtuosoXXXX
./configure --prefix=/opt/vos-6.1.2 --with-readline=/usr/lib/libreadline.so
make 
make install
```

_Optional parameters_
```
cd /tmp/virtuosoXXX
./configure --prefix=/opt/XXXX  --enable-php5=/tmp/php5 --with-iodbc=/tmp/iODBC  --with-readline=/usr/lib/libreadline.so
make 
make install
```