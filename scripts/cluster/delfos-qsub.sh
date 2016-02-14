#! /bin/bash
DELFOS_LOG_FILE='delfos-qsub.log'
rm $DELFOS_LOG_FILE

if [ ${#1} == 0 ]
then
   echo "Se debe especificar el directorio del experimento"
else
   q=0
   for i in $(ls  -d $1*/);
   do
      if [ $((q%3)) == 0 ]
      then
         qsub -q queue1 -v experimentFolder=${i} ./delfos-qsub-job.sh
         echo "qsub -q queue1 -v experimentFolder=${i} ./delfos-qsub-job.sh" >> $DELFOS_LOG_FILE
      else
         qsub -q queue2 -v experimentFolder=${i} ./delfos-qsub-job.sh
         echo "qsub -q queue2 -v experimentFolder=${i} ./delfos-qsub-job.sh" >> $DELFOS_LOG_FILE
      fi
      let q++
      sleep 1
   done
fi
