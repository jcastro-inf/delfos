#!/bin/bash
echo "Deleting unfinished jobs"
./pbs-delete-unfinished-jobs.sh
echo "Deleting unfinished done. Now delete running jobs"
./pbs-delete-running-jobs.sh

