#!/bin/sh

# script to checkout testAppsi
if [ $PWD != "$CODMON_HOME" ]
    then
    echo "Must be run in $CODMON_HOME !"
    exit 1
fi
for i in returnValue
do
    if [ -d $i ]
    then
	( cd $i && svn update )
    fi
done >testApps_update.out 2>&1

#remove the old testApps files.
rm -rf returnValue

#check out the testApps
svn checkout https://gforge.cs.vu.nl/svn/ibis/codmon/branches/berend/returnValue
