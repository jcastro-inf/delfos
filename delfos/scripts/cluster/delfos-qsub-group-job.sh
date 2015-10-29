#!/bin/bash

#PBS -m abe
#PBS -M jcastro@ujaen.es
#PBS -r y

#PBS -l nodes=1:ppn=12
hostname
cd $PBS_O_WORKDIR

idJob=${PBS_JOBID%%.*}

experimentFolderNoBars=${experimentFolder//"/"/.}

logDirectory=$experimentFolder"/log/"
if [ ! -d $logDirectory ]; then
   mkdir $logDirectory
fi

DATE_START=`date +%Y-%m-%d_%H.%M.%S`
logFileStd=$logDirectory$DATE_START"-log-std.txt"
logFileErr=$logDirectory$DATE_START"-log-err.txt"

echo "$DATE_START executing" $experimentFolder
echo "$DATE_START executing" $experimentFolder >> $logFileStd 2>> $logFileErr

echo "$DATE_START Executed in node `(hostname)`"
echo "$DATE_START Executed in node `(hostname)`" >> $logFileStd 2>> $logFileErr


~/java-8-oracle/bin/java -Xms16g -Xmx32g -jar delfos.jar --execute-group-xml -seed 123456 -directory ${experimentFolder} -num-exec $numExec >> $logFileStd 2>> $logFileErr

cat $logFileStd >&1
cat $logFileErr >&2


echo "$DATE_START executing" $experimentFolder
echo "$DATE_START executing" $experimentFolder >> $logFileStd 2>> $logFileErr
DATE_FINISH=`date +%Y-%m-%d_%H.%M.%S`
echo "$DATE_FINISH Finished" $experimentFolder
echo "$DATE_FINISH Finished" $experimentFolder >> $logFileStd 2>> $logFileErr
