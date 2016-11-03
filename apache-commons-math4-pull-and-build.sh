#!/bin/bash
set -e


utilsDirectory=$(pwd)


echo "Installing https://github.com/apache/commons-math.git in " $1

cd $1

commonsMath4Directory=$1"/commons-math"

echo $commonsMath4Directory "The directory"

if [ ! -d "$commonsMath4Directory" ]; then
	echo "Does not exists"
        git clone https://github.com/apache/commons-math.git
else
        echo "It exists, pull only"
	cd $commonsMath4Directory
        git pull origin master
fi

echo "Now i should compile"
cd $commonsMath4Directory

#Current commit in master fails, using 2016 june one.
git checkout dccc1255413adc1a212dbe3bf40accb70ccd44ef

mvn install -DskipTests

#Return to original directory
cd $utilsDirectory
