#! /bin/bash

if [ $# -ne 1 ] 
then
  echo "usage ./pack_releases.sh releas_number (x.y.z)"
  exit
fi

echo $1

release=AeonSDK-Java_$1.tgz
public=../releases/java
echo "Packing Java $release"
rm releases/$release
tar -cvzf releases/$release lib/AeonSDK-$1.jar lib/socketio.jar COPYRIGHT LICENSE LICENSE.more
echo "Copying to download public directory"
cp releases/$release $public
echo "Done it..."


