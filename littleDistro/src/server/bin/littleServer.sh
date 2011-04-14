#!/bin/sh
#
# ex: lgo help
#

cd `dirname $0`
cd ..
#date
command="java -Djava.util.logging.config.file=config/logging.properties -Dlittleware.home=config -Djava.security.auth.login.config=config/login.config -Dderby.system.home=data/javadb -Xmx512m -cp 'lib/*'  littleware.asset.server.bootstrap.CliServer $@"
#echo "Running command: " $command
eval "$command" 2>&1

