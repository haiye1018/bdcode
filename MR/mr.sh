#!/bin/sh
	if [ $# != 1 ]; then
		echo "use: ./mr.sh [basePath]"
		exit 1
	fi
	# /opt/data
	export BASEPATH=$1
	
	starttime=`date +'%Y-%m-%d %H:%M:%S'`
	
	# ITE00100554,17661214,TMAX,33,,,E,
	for zfile in $(ls $BASEPATH/*.csv.gz)
	do
		#echo $zfile
		gunzip -c $zfile | \
			awk -F ',' '{ temp = $4 + 0;
						  isMaxOrMin = $3;
						  if ( isMaxOrMin == "TMAX" && temp > max ) max = temp;
						  if ( isMaxOrMin == "TMIN" && temp < min ) min = temp }
					    END { print max,min}'
	done | awk -F ' ' '{ maxT = $1 + 0;
	                     minT = $2 + 0;
						 if ( maxT > max ) max = maxT;
						 if ( minT < min ) min = minT}
						END { print max,min }'
	
	endtime=`date +'%Y-%m-%d %H:%M:%S'`
	start_seconds=$(date --date="$starttime" +%s);
	end_seconds=$(date --date="$endtime" +%s);
	echo "Eclipsed Time： "$((end_seconds-start_seconds))"s"