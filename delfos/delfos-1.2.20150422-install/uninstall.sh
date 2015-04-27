#!/bin/bash

DELFOS_PATH="/usr/lib/delfos"
DELFOS_BIN="/usr/bin/delfos"

sudo rm $DELFOS_BIN -v
sudo rm -rvf $DELFOS_PATH/delfos

echo "Uninstall completed successfully"

