#!/bin/bash

DELFOS_LIB="/usr/lib/delfos"
DELFOS_BIN="/usr/bin/delfos"

sudo rm $DELFOS_BIN -v
sudo rm -rvf $DELFOS_LIB

echo "Uninstall completed successfully"

