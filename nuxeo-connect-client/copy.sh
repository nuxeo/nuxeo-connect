#!/bin/bash

SRC=`find src -name $1`
[ $? -eq 0 ] || exit 1
for file in $SRC; do
  SRC_ROOT=~/workspace/ant-assembly-maven-plugin/$(dirname $file)
  set -x
  mkdir -p $SRC_ROOT 
  cp -r $file $SRC_ROOT
  set +x
done
