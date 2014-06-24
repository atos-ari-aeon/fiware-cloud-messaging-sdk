#! /bin/bash

if [ $# -ne 1 ] 
then
  echo "usage ./pack_releases.sh releas_number (x.y.z)"
  exit
fi

cd java
./pack_release.sh $1
cd ..

cd javascript
./pack_release.sh $1
cd ..

cd nodejs
./pack_release.sh $1
cd ..

