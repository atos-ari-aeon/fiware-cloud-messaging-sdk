#! /bin/bash

if [ $# -ne 1 ] 
then
  echo "usage ./pack_releases.sh releas_number (x.y.z)"
  exit
fi

echo $1

release=AeonSDK-nodejs_$1.tgz
public=../../../rest/public/downloads/
echo "Packing nodejs $release"
cd aeonSDK
pack=`npm pack`
mv $pack $release
echo "Copying to download public directory"
cp $release $public
echo "Done it..."


