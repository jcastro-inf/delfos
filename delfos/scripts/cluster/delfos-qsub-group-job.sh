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

~/java-8-oracle/bin/java -Xms16g -Xmx32g -jar delfos.jar --execute-group-xml -seed 123456 -directory ${experimentFolder} -num-exec $numExec > $logFileStd 2> $logFileErr
