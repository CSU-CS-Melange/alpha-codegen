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
  --nox11 \
  --nochown \
  $BASE_DIR/../../artifact/bin \
  acc-installer.sh \
  "Alpha to C Compiler" \
  ./install-acc.sh

tar -cf acc-installer.sh.tgz acc-installer.sh

function get_latest_release {
  curl -L \
    -H "Accept: application/vnd.github+json" \
    -H "Authorization: Bearer $GITHUB_TOKEN" \
    -H "X-GitHub-Api-Version: 2022-11-28" \
    https://api.github.com/repos/csu-cs-melange/alpha-codegen/releases/latest 2> /dev/null
}

function upload_asset_blob {
  release_id=$1
  blob_file=$2
  curl -L \
    -X POST \
    -H "Accept: application/vnd.github+json" \
    -H "Authorization: Bearer $GITHUB_TOKEN" \
    -H "X-GitHub-Api-Version: 2022-11-28" \
    -H "Content-Type: application/octet-stream" \
    "https://uploads.github.com/repos/csu-cs-melange/alpha-codegen/releases/${release_id}/assets?name=${blob_file}" \
    --data-binary "@${blob_file}"
}

function get_latest_release_id {
  get_latest_release | jq '.id'
}

# upload to GitHub if token exists
if [[ -n "$GITHUB_TOKEN" ]]; then
  latest_id=`get_latest_release_id`
  if [[ "$latest_id" == "null" ]]; then
    echo "Could not find the latest release on remote, skipping upload step"
    exit 0
  fi
  upload_asset_blob $latest_id acc-installer.sh.tgz
fi

rm -f acc-installer.*
