#!/bin/bash
qstat queue2 | grep queue2 | egrep -o '[0-9][0-9][0-9][0-9][0-9][0-9]'| xargs qalter -W queue=queue1
