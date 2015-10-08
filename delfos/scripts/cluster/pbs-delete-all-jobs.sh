#!/bin/bash
qstat | egrep -o '[0-9][0-9][0-9][0-9][0-9][0-9]'| xargs qdel
