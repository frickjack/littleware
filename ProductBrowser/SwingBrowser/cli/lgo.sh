#!/bin/sh
# -max 3712603
# example: ./v2v.sh -solr /var/solr/vufind-1.0beta/solr -age 0 -min 3096600 -max 3706204 -age 0
#

launchdir=`/bin/dirname $0`
command="java -Xms1024m -Xmx1024m '-Djava.util.logging.config.file=${launchdir}/logging.properties' -cp '${launchdir}/SwingBrowser.jar' littleware.lgo.LgoCommandLine $@"
echo "Running command: " $command
eval "$command" 2>&1

