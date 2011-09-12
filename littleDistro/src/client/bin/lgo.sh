#!/bin/sh
#
# ex: lgo help
#

cd `dirname $0`
date
command="java '-Djava.util.logging.config.file=config/logging.properties' '-Dlittleware.home=./config' -Xmx512m -cp '../lib/*'  littleware.apps.lgo.LgoAssetCLI $@"
echo "Running command: " $command
eval "$command" 2>&1

