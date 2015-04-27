#!/bin/bash
set -e

DELFOS_PATH="/usr/lib/delfos"

mkdir $DELFOS_PATH
cp -rv ./* $DELFOS_PATH/

echo '#!/bin/bash
java -jar '$DELFOS_PATH'/delfos.jar -config '$HOME'/.config/delfos $@' > delfos

chmod +x delfos
mv ./delfos /usr/bin/ -v

if [ ! -f /usr/bin/delfos ]
then
	echo "This installer needs the right permissions to create the executable in /usr/bin/"
fi

#Generate the initial configuration of the library
delfos --initial-config

