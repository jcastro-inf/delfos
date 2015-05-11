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

#Download an install large datasets
datasetsSelected="-datasets-to-install ml-100k";
ml1mURL="http://files.grouplens.org/datasets/movielens/ml-1m.zip"

installml1m (){
	if [ ! -d "datasets/ml-1m" ]
	then 
		if [ ! -f "ml-1m.zip" ]
		then
			wget $ml1mURL
		fi
		unzip ml-1m.zip
		#rm ml-1m.zip
		mv ml-1m datasets/
	fi
}

while true; do
    read -p "Install ml-1m dataset? (it will be downloaded from grouplens.org) (Y/N)" yn
    case $yn in
        [Yy]* )
		echo "Install ml-1m dataset";
		installml1m
		datasetsSelected=$datasetsSelected" ml-1m"
		break;;
        [Nn]* )
		echo "User selected not to install ml-1m dataset.";
		break;;
        * ) echo "Please answer yes or no.";;
    esac
done

#Copy library binaries

chmod +r . -R
chmod +rx ./datasets/*
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
delfos --initial-config -datasets-dir $DELFOS_LIB/datasets $datasetsSelected --debug

echo "Cleaning the installation directory"
if [ -d "datasets/ml-1m" ]
then
	rm -r datasets/ml-1m
fi

if [ -f "ml-1m.zip" ]
then
	rm ml-1m.zip
fi

