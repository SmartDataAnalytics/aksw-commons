#!/bin/bash
echo "Enter the name of your user account (leave blank to skip)."
echo "If a name is given, login as 'akswadmin' will be disabled."
read username

echo "Enter the new desired hostname:"
read hostname

echo "Enter your IP suffix (between 1 and 253):"
read ipSuffix


echo "Setup a virtual hard drive on /dev/vdb? (no)"
read setUpHd

if [[ "$setUpHd" != "yes" ]]; then
    setUpHd="no"
if

echo ""
echo "Your settings are:"
echo "Username : $username"
echo "Hostname : $hostname"
echo "IP-suffix: $ipSuffix"
echo "Setup Hd : $setUpHd"

while [[ "$confirmation" != "yes" && "$confirmation" != "no" ]]; do
     echo "Are these settings ok (yes/no)?"
     read confirmation
done

if [[ "$confirmation" == "no" ]]; then
    exit 0
fi

if [ -n "$username" ]; then
	echo "Changing username not implemented"
fi

if [ -n "$hostname" ]; then
    .\ct-set-hostname.sh "$hostname"
fi

if [ -n "$ipSuffix" ]; then
    .\ct-ip-set.sh "$ipSuffix"
fi

if [ -n "$setUpHd" ]; then
    .\ct-format-all.sh
fi

