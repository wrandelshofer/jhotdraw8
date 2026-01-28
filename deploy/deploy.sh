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
export revision=0.5
mvn clean deploy -Dmaven.test.skip=true -Drevision=$revision
rm -r target/deploy
for module in org.jhotdraw8.*; do
  echo $module
  export moduledir=$module/ch/randelshofer/$module/$revision
  mkdir -p target/deploy/$moduledir
  cp $module/target/*.jar target/deploy/$moduledir
  cp $module/.flattened-pom.xml target/deploy/$moduledir/$module-$revision.pom
  for file in target/deploy/$moduledir/*.{jar,pom}; do
    gpg -ab "$file";
    sha1 -r "$file" | cut -f 1 -d " " > "$file.sha1";
    md5 -r "$file" | cut -f 1 -d " " > "$file.md5";
  done
  cd target/deploy/$module
  jar -cfM ../$module-$revision-bundle.jar .
  cd ../../..
done