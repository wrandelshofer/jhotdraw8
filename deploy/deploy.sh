#!/bin/bash
# We deploy to the nexus repository manager
#
# The nexus repository manager only accepts lower case character in the module name and in the version
# number.
#
# All files must be signed with GPG. We create a bundle.jar for each module, which we then
# can upload to the nexus repository manager.
#
cd "$(dirname "$0")"/..
export revision=0.1
mvn clean package -Dmaven.test.skip=true -Drevision=$revision
rm -r target/deploy
for module in org.jhotdraw8.*; do
  echo $module
  mkdir -p target/deploy/$module
  cp $module/target/*.jar target/deploy/$module
  cp $module/.flattened-pom.xml target/deploy/$module/$module-$revision.pom
  for file in target/deploy/$module/*.jar; do gpg -ab "$file"; done
  for file in target/deploy/$module/*.pom; do gpg -ab "$file"; done
  cd target/deploy/$module
  jar -cf ../$module-$revision-bundle.jar $(ls -1 $module*|xargs)
  cd ../../..
done