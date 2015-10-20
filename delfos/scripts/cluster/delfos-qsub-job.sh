#!/bin/bash

#PBS -m abe
#PBS -M jcastro@ujaen.es
#PBS -r y

#PBS -l nodes=1:ppn=12
hostname
cd $PBS_O_WORKDIR

idJob=${PBS_JOBID%%.*}

experimentFolderNoBars=${experimentFolder//"/"/.}

echo "Experimento $experimentFolder ejecutado aqui" >  $PBS_JOBNAME.o$idJob.$HOSTNAME.${experimentFolderNoBars}ejecutando
~/java-8-oracle/bin/java -Xms8g -jar delfos.jar --execute-xml -seed 123456 -directory ${experimentFolder} -num-exec 20
echo "Experimento $experimentFolder ejecutado aqui" >  $PBS_JOBNAME.o$idJob.$HOSTNAME.${experimentFolderNoBars}finalizado

