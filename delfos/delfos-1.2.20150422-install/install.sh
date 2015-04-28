#!/bin/bash
set -e

DELFOS_LIB="/usr/lib/delfos"
DELFOS_BIN="/usr/bin/delfos"
DELFOS_CONFIG=$HOME"/.config/delfos"

#Create shell command
echo '#!/bin/bash
DELFOS_LIB="'$DELFOS_LIB'"
DELFOS_JAR=$DELFOS_LIB"/delfos.jar"
DELFOS_BIN="/usr/bin/delfos"
DELFOS_CONFIG="'$HOME'/.config/delfos"
java -jar $DELFOS_JAR -config $DELFOS_CONFIG $@' > delfos
chmod +x delfos

#Copy library binaries
sudo mkdir $DELFOS_LIB
sudo cp -rv . $DELFOS_LIB

#Make delfos command available
sudo mv ./delfos $DELFOS_BIN -v


#Check correctness
if [ ! -f $DELFOS_BIN ]
then
	echo "This installer needs the right permissions to create the executable in /usr/bin/"
fi

#Generate the initial configuration of the library
delfos --initial-config -v -datasets-dir $DELFOS_LIB/datasets

