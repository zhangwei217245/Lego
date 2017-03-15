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
CLASS_NAME=x.spirit.App
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
DB=$1
echo "orientdb.url=remote:${DB}/test" > ${CNF_PATH}/orientdb_config.properties
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


start()
{
if test -e $PIDFILE
        then
                echo
                echo The $SERVER_NAME Server already Started!
                echo
        else
                echo
                echo Start The $SERVER_NAME Server.... $1 $2 $3 $4 $5
                echo
                
                $JAVA $PROGRAM_ARGS -classpath $CLASS_PATH $CLASS_NAME $1 $2 $3 $4 $5 >>$STDOUT 2>>$STDERR &
                echo $!>$PIDFILE
                sleep 2
                STATUS=`ps -p $!|grep java |awk '{print $1}'`
                if test $STATUS
                        then
                                echo The $SERVER_NAME Server Started!
                                echo
				startreading
                        else
                                rm $PIDFILE
				echo The $SERVER_NAME Server Start Failed
                                echo please Check the system
                                echo
                fi
fi
}

query()
{
if test -e $PIDFILE
        then
                echo
                echo The $SERVER_NAME Query already Started!
                echo
        else
                echo
                echo Start The $SERVER_NAME Query.... $1 $2 $3 $4
                echo
                $JAVA $PROGRAM_ARGS -classpath $CLASS_PATH $CLASS_NAME $1 $2 $3 $4

fi
}

stopread(){
	touch $PROCESSOR_HOME/stopread;
}

startreading(){
	rm -rf $PROCESSOR_HOME/stopread;
}

stop()
{
if test -e $PIDFILE
        then
		echo Stop reading...
		stopread
		sleep 10s
                echo
                echo Stop The $SERVER_NAME Server....
                echo
                TPID=`cat $PIDFILE`
                kill -9 $TPID
                sleep 1
                STATUS=`ps -p $TPID |grep java | awk '{print $1}'`
                if test $STATUS
                        then
                                echo The $SERVER_NAME Server NOT Stoped!
                                echo please Check the system
                                echo
                        else
                                echo The $SERVER_NAME Server Stoped
                                echo
                                rm $PIDFILE
                fi
        else
                echo
                echo The $SERVER_NAME Server already Stoped!
                echo
fi
}



status()
{
echo
if test -e $PIDFILE
        then
                TPID=`cat $PIDFILE`
                STATUS=`ps -p $TPID|grep java | awk '{print $1}'`
                if test $STATUS
                        then
                             #   echo "The $SERVER_NAME Server Running($TPID)!"
                                echo
                        else
                             #   echo The $SERVER_NAME Server NOT Running!
                                rm $PIDFILE
                                echo
                fi
        else
                echo
               # echo The $SERVER_NAME Server NOT Running!
                echo
fi
}

status

case "$1" in
'start')
                start $2 $3 $4 $5 $6
        ;;
'query')
                query $2 $3 $4 $5
        ;;
'stop')
                stop
        ;;
'status')
                status
        ;;
'stopread')
		stopread
	;;
'startreading')
		startreading
	;;
*)
        echo
        echo
        echo "Usage: $0 {status | start | query | stop | stopread | startreading }"
        echo
        echo Status of $SERVER_NAME Servers ......
                status
        ;;
esac
exit 0
