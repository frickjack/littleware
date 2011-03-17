#!/bin/sh
#
# ex: lgo help
#

cd `dirname $0`
date
command="java '-Djava.util.logging.config.file=config/logging.properties' -Xmx512m -cp '../lib/*'  littleware.lgo.LgoCommandLine $@"
echo "Running command: " $command
eval "$command" 2>&1
