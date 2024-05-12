#!/bin/bash

# This script bundles the acc script and the two jars into a single executable
# shell script. This currently uses a utility called "makeself". This utility 
# is available here:
# -  https://github.com/megastep/makeself
#

BASE_DIR=`dirname -- $0`

if [[ ! -f "$BASE_DIR/../../artifact/bin/alpha.glue.v1.jar" ]]; then
  echo "[error] cannot find the 'alpha.glue.v1.jar' file, exiting"
  exit 1
fi

if [[ ! -f "$BASE_DIR/../../artifact/bin/alpha.glue.v2.jar" ]]; then
  echo "[error] cannot find the 'alpha.glue.v2.jar' file, exiting"
  exit 1
fi

if [[ -z "$(which makeself)" ]]; then
  echo "[error] this script requires 'makeself, exiting'"
  exit 1
fi

# call makeself to bundle everything
makeself \
  --nocomp \
  --current \
  --nox11 \
  --nochown \
  --nomd5 \
  --nocrc \
  $BASE_DIR/../../artifact/bin \
  acc \
  "Alpha to C Compiler (acc)" \
  ./acc

# move it to the repo root directory
mkdir -p /usr/local/bin/
cp acc /usr/local/bin/
