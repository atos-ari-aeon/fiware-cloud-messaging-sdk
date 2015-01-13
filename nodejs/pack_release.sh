#! /bin/bash

if [ $# -ne 1 ] 
then
  echo "usage ./pack_releases.sh release_number (x.y.z)"
  echo "Please ensure that release number is the same in package.json"
  exit
fi

if [ $(grep -c $1 ./aeonSDK/package.json) -eq 0 ]
then
  echo "Trying to package with a release number different than package.json, please check"
  exit
fi

release=AeonSDK-nodejs_$1.tgz
public=../../releases/nodejs
echo "Packing nodejs $release"
cd aeonSDK
pack=`npm pack`
mv $pack $release
echo "Copying to download public directory"
cp $release $public
echo "Done it..."


