#!/bin/bash

readonly master_pom=releng/org.dice.deployments.configuration/pom.xml
readonly osgi_files="
  bundles/org.dice.deployments.client/META-INF/MANIFEST.MF
  bundles/org.dice.deployments.datastore/META-INF/MANIFEST.MF
  bundles/org.dice.deployments.ui/META-INF/MANIFEST.MF
  features/org.dice.deployments.client.feature/feature.xml
  features/org.dice.deployments.datastore.feature/feature.xml
  features/org.dice.deployments.ui.feature/feature.xml
  releng/org.dice.deployments.update/category.xml
"
readonly mvn_files="
  bundles/pom.xml
  features/pom.xml
  pom.xml
  releng/org.dice.deployments.configuration/pom.xml
  releng/org.dice.deployments.update/pom.xml
  releng/pom.xml
"


function usage()
{
  cat <<EOF >&2
$0 - Script for updating version of the IDE plugin

Usage:
  $0 NEW_VERSION
    NEW_VERSION should follow semantic versioning scheme without qualifiers.

Examples:
  $0 0.3.1

EOF

  [[ -n $1 ]] && echo "Error: $1" >&2

  exit 1
}

function validate_version()
{
  local semver_re='^(([1-9][0-9]*)|0)\.(([1-9][0-9]*)|0)\.(([1-9][0-9]*)|0)$'
  echo $1 | grep -E $semver_re
}

function extract_current_version()
{
  xpath -q -e '/project/version/text()' $master_pom | cut -d- -f1
}

function replace_osgi_version()
{
  for f in $osgi_files
  do
    sed -i -e "s/${1}.qualifier/${2}.qualifier/g" $f
  done
}

function replace_mvn_version()
{
  for f in $mvn_files
  do
    sed -i -e "s/${1}-SNAPSHOT/${2}-SNAPSHOT/g" $f
  done
}

function main()
{
  local old_version=$(extract_current_version)
  local new_version=$1

  replace_osgi_version $old_version $new_version
  replace_mvn_version $old_version $new_version
}

[[ -z "$1" ]] && usage "Missing version argument"
validate_version $1 || usage "Invalid version $1"

main $1
