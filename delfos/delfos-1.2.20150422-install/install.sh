#!/bin/bash
set -e

DELFOS_PATH="/usr/lib/delfos"
DELFOS_BIN="/usr/bin/delfos"

#Copy library binaries
sudo mkdir $DELFOS_PATH
sudo cp -rv ./* $DELFOS_PATH/


#Create shell command
echo '#!/bin/bash
java -jar '$DELFOS_PATH'/delfos.jar -config '$HOME'/.config/delfos $@' > delfos
chmod +x delfos

#Make delfos command available
sudo mv ./delfos $DELFOS_BIN -v


#Check correctness
if [ ! -f $DELFOS_BIN ]
then
	echo "This installer needs the right permissions to create the executable in /usr/bin/"
fi

#Generate the initial configuration of the library
delfos --initial-config

