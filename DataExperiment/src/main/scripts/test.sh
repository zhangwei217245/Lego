#!/bin/sh
#
#
PRG="$0"

while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

PRGDIR=`dirname "$PRG"`
[ -z "$PROCESSOR_HOME" ] && PROCESSOR_HOME=`cd "$PRGDIR/.." ; pwd`

SERVER_NAME=MESHWORK_SERVER

# path
BIN_PATH=${PROCESSOR_HOME}/bin
LOG_PATH=${PROCESSOR_HOME}/logs
LIB_PATH=${PROCESSOR_HOME}/lib
CNF_PATH=${PROCESSOR_HOME}/conf
#
if [ ! -d $LOG_PATH ];then
    mkdir $LOG_PATH
fi

#
CLASS_NAME=x.spirit.TestOrientDB
CLASS_PATH=$CNF_PATH
#
for f in $LIB_PATH/*.jar
do
    CLASS_PATH=$CLASS_PATH:$f;
done

#DEBUG_ARGS="-agentlib:jdwp=transport=dt_socket,address=8759,server=y,suspend=y";
DEBUG_ARGS="";
#
PROGRAM_ARGS="-Xms2g -Xmx2g  -Xmn1g -DServer=mqproc -Dprocess.home=${PROCESSOR_HOME} -XX:+UseConcMarkSweepGC -server -XX:SurvivorRatio=5 -XX:CMSInitiatingOccupancyFraction=80 -XX:+PrintTenuringDistribution  -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCApplicationConcurrentTime ${DEBUG_ARGS} -Xloggc:./gc.log"


PROC_ID=$1
shift
#
STDOUT=$LOG_PATH/stdout${PROC_ID}.log
STDERR=$LOG_PATH/stderr${PROC_ID}.log
touch $LOG_PATH/stdout${PROC_ID}.log
touch $LOG_PATH/stdout${PROC_ID}.log
#STDOUT=/dev/null
#STDERR=/dev/null

PIDFILE=$BIN_PATH/udf${PROC_ID}.pid

JAVA=`which java`


$JAVA $PROGRAM_ARGS -classpath $CLASS_PATH $CLASS_NAME $1
echo $!>$PIDFILE