
#!/bin/bash

EXPECTED_ARGS=1

if [ $# -ne $EXPECTED_ARGS ]
  then
    echo "Give the name of the sensor. for example basics"
    exit
fi

set -x

export CODMON_HOME=$HOME/codmon
export JAVA_HOME=/home/ceriel/jdk1.7.0_04
#export JAVA_HOME=/usr/java/jdk1.7.0_04

#TODO check If this can be made dynamically
export IPL_HOME=$CODMON_HOME/ibis
export MPJ_HOME=$CODMON_HOME/ibis-mpj
export RMI_HOME=$CODMON_HOME/ibis-rmi
export GMI_HOME=$CODMON_HOME/ibis-gmi
export SATIN_HOME=$CODMON_HOME/satin
export RETURNVALUE_HOME = $CODMON_HOME/returnValue

cd $CODMON_HOME/codmon

mkdir -p dday dday1 dday2 dday3

cat > dday1/allin1.xml <<EOF
<?xml version="1.0" encoding="utf-8"?>
<?xml-stylesheet type="text/xsl" href="../libs/merge.xml"?>
<xml/>
EOF

#TODO Copy only the necesary files
cp dday2/*.xml dday3
cp dday1/*.xml dday2
cp dday/*.xml dday1

cd $CODMON_HOME/codmon/build
echo "Running at" `date` >> cron
#$JAVA_HOME/bin/java -Djava.awt.headless=true -jar codmon.jar ../sensors-basics.xml ../dday/shot-basics.xml >cronlog 2>&1
$JAVA_HOME/bin/java -Djava.awt.headless=true -jar codmon.jar ../sensors-$1.xml ../dday/shot-$1.xml >cronlog 2>&1

if [ $? != "0" ]; then
	echo "Oops something went wrong"
	exit $?
fi

if [ -e "$CODMON_HOME/ibis-apps" ]; then
    if [ -e "$CODMON_HOME/ipl-apps" ]; then
        cd $CODMON_HOME/codmon/libs
        perl gather_tests.pl ../sensors tcp $CODMON_HOME/ibis-apps $CODMON_HOME/ipl-apps
    fi
fi

#cd $CODMON_HOME/codmon/build

#$JAVA_HOME/bin/java -Djava.awt.headless=true -jar codmon.jar ../sensors-tcp.xml ../dday/shot-tcp.xml >>cronlog 2>&1
#if [ $? != "0" ]; then
#    exit $?
#fi
