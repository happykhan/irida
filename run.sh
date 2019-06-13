#!/bin/bash
export MIN_JS=false

sshfs -o allow_other,umask=777 centos@137.205.69.127:/export /export

ADD=""

if [ "$1" = "--create-db" ]; then
   ADD="-Dhbm.dev.auto=create"
   echo "Dropping then Creating/Recreating database schema"
else
   echo "Updating database schema without dropping"
  fi

mvn clean jetty:run  -e -Dspring.profiles.active=prod ${ADD}
