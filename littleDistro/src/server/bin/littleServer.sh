#!/bin/sh
#
# ex: lgo help
#

cd `dirname $0`
#date
command="java -Djava.util.logging.config.file=config/logging.properties -Dlittleware.home=config -Djava.security.auth.login.config=config/login.config -Dderby.home=data/javadb -Xmx512m -cp 'lib/*'  littleware.bootstrap.server.CliServer $@"
echo "Running command: " $command
eval "$command" 2>&1

