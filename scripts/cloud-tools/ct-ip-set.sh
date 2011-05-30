#!/bin/bash
newIpv4=$1
newIpv6=`echo "$1" | tr '.' ':'`

if [ -z "$1" ]; then
        echo "No ip given."
        exit 1
fi

baseIpv4="192.168.35.110"
baseIpv6="2001:638:902:2010:0:168:35:110"


#echo "$baseIpv4"

newIpv4=`./ct-ip-merge.sh "$baseIpv4" "$newIpv4" "."`
newIpv6=`./ct-ip-merge.sh "$baseIpv6" "$newIpv6" ":"`


#echo $newIpv4
#echo $newIpv6

cat /etc/network/interfaces | sed -r "s/address\s+(([0-9]+)(\.[0-9]+)+)/address $newIpv4/g" | sed -r "s/address\s(([0-9]+)(:[0-9]*)+)/address $newIpv6/g" > /etc/network/interfaces

sudo ifdown eth0
sudo ifup eth0

#cat /etc/network/interfaces | sed -r "s/address(\s+(\d\.)+)/$newIpv4/g" address $newIpv4
#context=""
#for x in `cat /tmp/test`; do
#	echo "line: $x"
#	interface=`echo "$x" | sed -rn "s/iface\s+([^\s]+)\s+/\1/p"`

#	echo "int = $interface"
#	if [ -n $interface ]; then
		context=$interface
#	fi

#	if [ "$context" == "eth0" ]; then
#		x=`echo "$x" | sed -r "s/address\s+(([0-9]+)(\.[0-9]+)+)/address $newIpv4/g"`
#		x=`echo "$x" | sed -r "s/address\s(([0-9]+)(:[0-9]*)+)/address $newIpv6/g"`
#	fi

#	echo "$context: $x"
#done
	

#done


# | sed -r "s/iface eth0(?=iface).*/test/g"


