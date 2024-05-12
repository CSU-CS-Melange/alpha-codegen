#!/bin/bash

BASE_DIR="$(dirname -- $0)"

mkdir -p $HOME/.acc/bin

cp $BASE_DIR/acc $HOME/.acc/bin/
cp $BASE_DIR/*.jar $HOME/.acc/bin/
