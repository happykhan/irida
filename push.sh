#!/bin/bash
mvn clean package -DskipTests
cp target/irida-*-SNAPSHOT.war  ../server-manager/files/irida-latest.war
