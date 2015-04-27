#!/bin/bash

right_directory=false

while [ $right_directory = false ]
do
   echo -n "file or directory name: " # 2. prompt for name of file or directory
   read HANDLE # ...  and read it
   if [ -r "$HANDLE" ] # 2. b - check if it exists and is readable
   then
      right_directory=true
   else
      echo "$HANDLE is not readable";    # if not, exit with an exit code != 0
   fi
done
