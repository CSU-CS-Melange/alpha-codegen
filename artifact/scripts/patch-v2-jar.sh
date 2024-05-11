#!/bin/bash

set -e

JAR_DIR=$(realpath `dirname -- $1`)
JAR=`basename -- $1`
rm -rf .tmp/*
mkdir -p .tmp
mv $JAR_DIR/$JAR .tmp/
cd .tmp/
jar -xf $JAR
rm -f $JAR
echo _UI_DiagnosticRoot_diagnostic=_UI_DiagnosticRoot_diagnostic > plugin.properties
jar -cf $JAR_DIR/$JAR *
cd ../
rm -rf .tmp
