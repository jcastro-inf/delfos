#! /bin/bash

if [ $# == 0 ]
then
   echo "Se debe especificar el directorio del experimento"
elif [ $# == 1 ]
then
   echo "Se debe especificar el número de ejecuciones"
elif [ $# == 2 ]
then
   if [[ $2 =~ ^[0-9]+$ ]]
   then
      DELFOS_LOG_FILE='delfos-qsub-group.log'
      rm $DELFOS_LOG_FILE

      q=0
      for i in $(ls  -d $1*/);
      do
         if [ $((q%3)) == 0 ]
         then
            qsub -q queue1 -v experimentFolder=${i},numExec=$2 ./delfos-qsub-group-job.sh;
            echo "qsub -q queue1 -v experimentFolder=${i},numExec=$2 ./delfos-qsub-group-job.sh" >> $DELFOS_LOG_FILE
         else
            qsub -q queue2 -v experimentFolder=${i},numExec=$2 ./delfos-qsub-group-job.sh;
            echo "qsub -q queue2 -v experimentFolder=${i},numExec=$2 ./delfos-qsub-group-job.sh" >> $DELFOS_LOG_FILE
         fi
         let q++
         sleep 0.2s
      done
   else
      echo "El segundo parametro debe ser un numero de ejecuciones: '$2' no es válido"
   fi
else
   echo "Demasiados argumentos, sólo hay que indicar directorio y numero de ejecuciones"
fi
