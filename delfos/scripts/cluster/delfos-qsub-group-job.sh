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

DATE=`date +%Y-%m-%d_%H.%M.%S`
logFileStd=$logDirectory$DATE"-log-std.txt"
logFileErr=$logDirectory$DATE"-log-err.txt"

echo "$DATE executing" $experimentFolder
echo "$DATE executing" $experimentFolder >> $logFileStd 2>> $logFileErr

echo "$DATE Executed in node `(hostname)`"
echo "$DATE Executed in node `(hostname)`" >> $logFileStd 2>> $logFileErr


~/java-8-oracle/bin/java -Xms16g -Xmx32g -jar delfos.jar --execute-group-xml -seed 123456 -directory ${experimentFolder} -num-exec $numExec >> $logFileStd 2>> $logFileErr


echo "$DATE Finished" $experimentFolder
echo "$DATE Finished" $experimentFolder >> $logFileStd 2>> $logFileErr
