#!/bin/bash

DELFOS_PATH="/usr/lib/delfos"
DELFOS_BIN="/usr/bin/delfos"

sudo rm $DELFOS_BIN -v
sudo rm -rf $DELFOS_PATH/delfos -v

echo "Uninstall completed successfully"

