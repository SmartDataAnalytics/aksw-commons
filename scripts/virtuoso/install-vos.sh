#!/bin/bash
# Simple script for building and installing virtuoso open source from source
# Usage: install-vos.sh [version]
# Example: ./install-vos.sh 6.1.5

version=$1

if [ -z "$version" ]; then
    echo "Please specify the virtuoso open source version to install. e.g. 6.1.5"
    exit 1
fi

sudo apt-get install autoconf automake libtool flex bison gperf gawk m4 make openssl libssl-dev libreadline-dev

#For centos (and the likes)
#sudo yum install autoconf automake flex bison gperf gawk m4 make readline-devel openssl-devel 

wget "http://downloads.sourceforge.net/project/virtuoso/virtuoso/$version/virtuoso-opensource-$version.tar.gz"

tar -zxvf "virtuoso-opensource-$version.tar.gz"
cd "virtuoso-opensource-$version"

./configure --prefix="/opt/virtuoso/ose/$version" --with-readline=/usr/lib/libreadline.so
make -j
make install
