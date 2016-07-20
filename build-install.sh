#!/bin/bash
set -e

delfosDirectory=$(pwd)

cd ..
utilsDirectory=$(pwd)/delfos.util
rm -r $utilsDirectory
mkdir $utilsDirectory

cd $delfosDirectory

./apache-commons-math4-pull-and-build.sh $utilsDirectory

cd $delfosDirectory

mvn clean install -DskipTests

cp target/delfos-jar-with-dependencies.jar delfos-install/delfos.jar

cd delfos-install/
./uninstall.sh
./install.sh
cd ..
read -rsp $'Install finished, press any key to close.\n' -n 1;
