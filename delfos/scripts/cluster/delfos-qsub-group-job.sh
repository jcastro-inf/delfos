#!/bin/bash

#PBS -m abe
#PBS -M jcastro@ujaen.es
#PBS -r y

#PBS -l nodes=1:ppn=5
hostname
cd $PBS_O_WORKDIR

#Copio el archivo de configuración general del cluster para ser usado en este nodo.
cp ./.config/configuredDatasets@delfos0.xml ./.config/configuredDatasets@$HOSTNAME.xml -v

idJob=${PBS_JOBID%%.*}

experimentFolderNoBars=${experimentFolder//"/"/.}
resultsFile=${experimentFolder}results/aggregateResults.xls

if [ -a $resultsFile ] 
then
	echo "Experimento $experimentFolder ejecutado aqui" >  $PBS_JOBNAME.o$idJob.$HOSTNAME.${experimentFolderNoBars}.yaHabiaSidoEjecutado	
else
	echo "Experimento $experimentFolder ejecutado aqui" >  $PBS_JOBNAME.o$idJob.$HOSTNAME.${experimentFolderNoBars}ejecutando
	~/java-8-oracle/bin/java -Xms8g -jar delfos.jar --execute-group-xml -seed 77352653 -directory ${experimentFolder} -num-exec 20
	echo "Experimento $experimentFolder ejecutado aqui" >  $PBS_JOBNAME.o$idJob.$HOSTNAME.${experimentFolderNoBars}finalizado
fi
