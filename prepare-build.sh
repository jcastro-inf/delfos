#!/bin/bash
set -e

delfosDirectory=$(pwd)

cd ..
utilsDirectory=$(pwd)/delfos.util

mkdir $utilsDirectory -p

cd $delfosDirectory

./apache-commons-math4-pull-and-build.sh $utilsDirectory

cd $delfosDirectory

