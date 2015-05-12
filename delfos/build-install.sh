#!/bin/bash
set -e

mvn clean install -DskipTests

cp target/delfos-jar-with-dependencies.jar delfos-install/delfos.jar

cd delfos-install/
./uninstall.sh
./install.sh
cd ..
