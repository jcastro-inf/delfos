#!/bin/bash

#PBS -m abe
#PBS -M jcastro@ujaen.es
#PBS -r y

#PBS -l nodes=1:ppn=12
hostname
cd $PBS_O_WORKDIR

idJob=${PBS_JOBID%%.*}

experimentFolderNoBars=${experimentFolder//"/"/.}
resultsFile=${experimentFolder}results/aggregateResults.xls

if [ -a $resultsFile ] 
then
   echo "Experimento $experimentFolder ejecutado aqui" >  $PBS_JOBNAME.o$idJob.$HOSTNAME.${experimentFolderNoBars}.yaHabiaSidoEjecutado   
else
   echo "Experimento $experimentFolder ejecutado aqui" >  $PBS_JOBNAME.o$idJob.$HOSTNAME.${experimentFolderNoBars}ejecutando
   ~/java-8-oracle/bin/java -Xms8g -jar delfos.jar --execute-group-xml -seed 123456 -directory ${experimentFolder} -num-exec $numExec
   echo "Experimento $experimentFolder ejecutado aqui" >  $PBS_JOBNAME.o$idJob.$HOSTNAME.${experimentFolderNoBars}finalizado
fi
