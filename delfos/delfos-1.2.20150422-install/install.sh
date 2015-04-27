#!/bin/bash
set -e

DELFOS_PATH="/usr/lib/delfos"

mkdir $DELFOS_PATH
cp ./* $DELFOS_PATH/ -v

echo '#!/bin/bash
java -jar '$DELFOS_PATH'/delfos.jar -config '$DELFOS_PATH'/.config/delfos $@' > delfos

chmod +x delfos
mv delfos /usr/bin/delfos -v

if [ ! -f /usr/bin/delfos ]
then
	echo "This installer needs the right permissions to create the executable in /usr/bin/"
fi

#Generate the initial configuration of the library
delfos --initial-config -config $DELFOS_PATH/.config/delfos

