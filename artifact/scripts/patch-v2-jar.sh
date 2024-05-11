#!/bin/bash

set -e

function patch_jar {
    JAR_DIR=`dirname -- $1`
    JAR=`basename -- $1`
    rm -rf tmp/*
    mkdir -p tmp
    mv $JAR_DIR/$JAR tmp/
    cd tmp/
    jar -xf $JAR
    rm -f $JAR
    echo _UI_DiagnosticRoot_diagnostic=_UI_DiagnosticRoot_diagnostic > plugin.properties
    jar -cf ../jars/$JAR *
    cd ../
    rm -rf tmp
}

patch_jar jars/alpha.glue.v2.jar
