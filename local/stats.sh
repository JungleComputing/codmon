#!/bin/bash

export JAVA_HOME=/usr/java/jre1.6.0_05
export IBIS_HOME=$HOME/ibis

. $HOME/.bashrc

cd $HOME/codmon
cp day2/*.xml day3
cp day1/*.xml day2
cp dday/*.xml day1

cd $HOME/codmon/codmon/build
#echo "Running at" `date` >> cron
$JAVA_HOME/bin/java -Djava.awt.headless=true -jar codmon.jar ../sensors-basics.xml ../dday/shot-basics.xml >cronlog 2>&1
if [ $? != "0" ]; then
    exit $?
fi

if [ -e "$HOME/ibis-apps" ]; then
    if [ -e "$HOME/ipl-apps" ]; then
        cd $HOME/codmon/libs
        perl gather_tests.pl ../sensors tcp $HOME/ibis-apps $HOME/ipl-apps
    fi
fi

cd $HOME/codmon/codmon/build

$JAVA_HOME/bin/java -Djava.awt.headless=true -jar codmon.jar ../sensors-tcp.xml ../dday/shot-tcp.xml >>cronlog 2>&1
if [ $? != "0" ]; then
    exit $?
fi
