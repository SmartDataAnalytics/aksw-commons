#!/bin/bash
echo "Enter the name of your user account (leave blank to skip)."
echo "If a name is given, login as 'akswadmin' will be disabled."
read username

echo "Enter the new desired hostname:"
read hostname

echo "Enter your IP suffix (between 1 and 253):"
read ipSuffix


echo ""
echo "Your settings are:"
echo "Username : $username"
echo "Hostname : $hostname"
echo "IP-suffix: $ipSuffix"

while [[ "$confirmation" != "yes" && "$confirmation" != "no" ]]
do
     echo "Are these settings ok (yes/no)?"
     read confirmation
done

if [ -z "$username" ]; then
	echo "blah"
fi


