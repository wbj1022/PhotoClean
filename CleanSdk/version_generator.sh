#!/bin/bash
localpath=$1
verisonfile=$localpath/src/com/vivo/secureplus/Version.java
#echo "gener the version file begin......"
#echo $verisonfile
rm -rf $verisonfile
lastcommitid="`git --git-dir=$localpath/.git log  --oneline -1 | awk '{print $1}' `"
currentbranch="`git --git-dir=$localpath/.git branch | awk  '/\*/{print $0}' | awk -F* '{print $2}' | awk '{sub("^ *","");sub(" *$","");print}'`"
#echo "commit id is === "$lastcommitid
#echo "currentbranch is === "$currentbranch

echo -e "package com.vivo.secureplus;\n\npublic final  class Version{\n\tpublic final static String GIT_VERSION = \"$lastcommitid\";\n\tpublic final static String CURRENT_BRANCH = \"$currentbranch\";\n}" > $verisonfile 
