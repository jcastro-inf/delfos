#!/bin/bash
set -e

cp target/delfos-jar-with-dependencies.jar delfos-install/delfos.jar

cd delfos-install/
./uninstall.sh
./install.sh
cd ..
read -rsp $'Install finished, press any key to close.\n' -n 1;