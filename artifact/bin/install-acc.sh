#!/bin/bash

BASE_DIR="$(dirname -- $0)"

mkdir -p $HOME/.acc/bin

cp $BASE_DIR/acc $HOME/.acc/bin/
cp $BASE_DIR/*.jar $HOME/.acc/bin/

echo
echo "Additional steps may be required. The (A)lpha to (C) (C)ompiler has been"
echo "installed to the following location:"
echo
echo "    $HOME/.acc/bin"
echo
echo "You may wish to add this to you path with the following command:"
echo
echo '    export PATH=$PATH:$HOME/.acc/bin'
