#!/bin/bash

dev="$1"
partition="$dev1"
mountPoint="$2"

if [ -z "$dev" ]; then
        echo "No device given."
        exit 1
fi

if [ -z "$mountPoint" ]; then
        echo "No mount point given."
        exit 1
fi


sudo fdisk "$dev" < data/create-partition.fdisk
mkfs -t ext4 "$partition"

uuid=`blkid -o value -s UUID "$partition"`

entry="UUID=$uuid $mountPoint               ext4    errors=remount-ro 0       1"
echo $entry >> /etc/fstab
mount "$partition" "$mountPoint"
