#!/bin/sh
# -max 3712603
# example: ./v2v.sh -solr /var/solr/vufind-1.0beta/solr -age 0 -min 3096600 -max 3706204 -age 0
#

cd `dirname $0`
date
command="java -Xms1024m -Xmx1024m -cp SwingBrowser.jar littleware.apps.tracker.browser.CliApp $@"
echo "Running command: " $command
eval "$command" 2>&1

