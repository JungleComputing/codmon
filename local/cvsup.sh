#!/bin/sh

# Simple script to checkout the last Ibis version

if [ $PWD != "/home1/codmon" ]
    then
    echo "Must be run in /home1/codmon !"
    exit 1
fi
for i in ibis ibis-rmi ibis-gmi ibis-mpj satin
do
    if [ -d $i ]
    then
	( cd $i && svn update )
    fi
done > update.out 2>&1

rm -rf ibis
rm -rf ibis-mpj
rm -rf ibis-rmi
rm -rf ibis-gmi
rm -rf satin
rm -rf ibis-apps
rm -rf ipl-apps

svn checkout https://gforge.cs.vu.nl/svn/ibis/ipl/trunk ibis
svn checkout https://gforge.cs.vu.nl/svn/ibis/mpj/trunk ibis-mpj
svn checkout https://gforge.cs.vu.nl/svn/ibis/rmi/trunk ibis-rmi
svn checkout https://gforge.cs.vu.nl/svn/ibis/gmi/trunk ibis-gmi
svn checkout https://gforge.cs.vu.nl/svn/ibis/satin/trunk satin
svn export https://gforge.cs.vu.nl/svn/ibis/apps/trunk ibis-apps
svn export https://gforge.cs.vu.nl/svn/ibis/ipl-apps/trunk ipl-apps
