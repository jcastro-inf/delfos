#! /bin/bash
if [ ${#1} == 0 ]
then
	echo "Se debe especificar el directorio del experimento"
else	
	if [ ${#1} == 0 ] || [ $2 -le 0 ]
	then
		echo "Se debe especificar el numero de tareas de cada experimento"
	else
		q=0
		for i in $(ls  -d $1*/);
		do
			#read -p "Press [Enter] key to submit $2 jobs running on '"$i"' experiment"
			k=1
			while [ $k -le $2 ] 
			do
				if [ $((q%3)) == 0 ]
				then
					qsub -q queue1 -v experimentFolder=${i} "./delfos-qsub-group-job.sh";echo "submited to queue1"
					echo 'qsub -q queue1 -v experimentFolder=${i} "./delfos-qsub-group-job.sh";echo "submited to queue1' >> delfos-qsub-group.sh.log
				else
					qsub -q queue2 -v experimentFolder=${i} "./delfos-qsub-group-job.sh";echo "submited to queue2"
					echo 'qsub -q queue2 -v experimentFolder=${i} "./delfos-qsub-group-job.sh";echo "submited to queue2' >> delfos-qsub-group.sh.log
				fi
				let k++
				let q++
			done
			sleep 1
		done
	fi
fi
