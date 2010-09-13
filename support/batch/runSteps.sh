#!/bin/bash 

export JAVA_HOME=/opt/kuali/apps/jdk1.5.0_15 
export PATH=$PATH:/opt/kuali/apps/apache-ant-1.8.1/bin 

/opt/kuali/apps/apache-ant-1.8.1/bin/ant run-step -Dbuild.environment=$1 -Dstep.name=$2 