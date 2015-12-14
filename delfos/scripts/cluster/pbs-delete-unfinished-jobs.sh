#!/bin/bash
qstat | grep sinbad2 | grep -v R | egrep -o '[0-9][0-9][0-9][0-9][0-9][0-9]'| while read -r jobId ; do
   qdel $jobId
   echo "deleted job $jobId"
   sleep 0.2
done
