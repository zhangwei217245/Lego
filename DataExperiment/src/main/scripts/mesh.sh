#!/bin/sh

# mesh query
# check jar not duplicate
#cd ../lib;
#jars=('apiCommons*' 'apiData*' 'apiEngine*' 'mblogapi*');
#for (( i = 0 ; i < ${#jars[@]} ; i++ )) do
#	jarName="${jars[$i]}";
#	num=`ls -l $jarName | wc -l`;
#	if [ "${num}" = "2" ]; then
#		echo '[Launch ERROR] jar dumplicate:'$jarName;
#		exit;
#	else
#		echo "[Launch OK] "$jarName;
#	fi
#done
cd ../bin;
echo $1 $2 $3 $4 $5
if [[ "x$1" == "x-q" ]]; then
sh ./dataImportConsole.sh query "$@"
fi

if [[ "x$1" == "x-f" ]]; then
sh ./dataImportConsole.sh start "$@"
fi