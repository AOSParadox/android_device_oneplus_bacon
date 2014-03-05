#!/bin/bash

set -e

BASE=../../../vendor/$VENDOR/$DEVICE/proprietary
rm -rf $BASE/*

for FILE in `egrep -v '(^#|^$)' ../../oppo/msm8974-common/proprietary-files.txt` `egrep -v '(^#|^$)' ../../oppo/msm8974-common/proprietary-files-qc.txt`; do
  OLDIFS=$IFS IFS=":" PARSING_ARRAY=($FILE) IFS=$OLDIFS
  FILE=`echo ${PARSING_ARRAY[0]} | sed -e "s/^-//g"`
  DEST=${PARSING_ARRAY[1]}
  if [ -z $DEST ]
  then
    DEST=$FILE
  fi
  DIR=`dirname $FILE`
  if [ ! -d $BASE/$DIR ]; then
    mkdir -p $BASE/$DIR
  fi
  # Try CM target first
  adb pull /system/$DEST $BASE/$DEST
  # if file does not exist try OEM target
  if [ "$?" != "0" ]
  then
    adb pull /system/$FILE $BASE/$DEST
  fi
done

./../../oppo/msm8974-common/setup-makefiles.sh
