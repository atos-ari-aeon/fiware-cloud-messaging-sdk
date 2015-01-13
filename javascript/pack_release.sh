#! /bin/bash

if [ $# -ne 1 ] 
then
  echo "usage ./pack_releases.sh releas_number (x.y.z)"
  exit
fi

echo $1

release=AeonSDK-Javascript_$1.tgz
public=../releases/javascript
echo "Packing Javascrpit $release"
rm releases/$release
tar -cvzf releases/$release src/aeonSDK.js src/socket.io.js COPYRIGHT LICENSE LICENSE.more
echo "Copying to download public directory"
cp releases/$release $public
echo "Done it..."


