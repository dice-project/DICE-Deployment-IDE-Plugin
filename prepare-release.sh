#!/bin/bash

readonly master_pom=releng/org.dice.deployments.configuration/pom.xml
readonly osgi_files="
  bundles/org.dice.deployments.client/META-INF/MANIFEST.MF
  bundles/org.dice.deployments.datastore/META-INF/MANIFEST.MF
  bundles/org.dice.deployments.standalone/META-INF/MANIFEST.MF
  bundles/org.dice.deployments.ui/META-INF/MANIFEST.MF
  features/org.dice.deployments.client.feature/feature.xml
  features/org.dice.deployments.datastore.feature/feature.xml
  features/org.dice.deployments.standalone.feature/feature.xml
  features/org.dice.deployments.ui.feature/feature.xml
  releng/org.dice.deployments.update.ide/category.xml
  releng/org.dice.deployments.update.standalone/category.xml
"
readonly mvn_files="
  bundles/pom.xml
  features/pom.xml
  pom.xml
  releng/org.dice.deployments.configuration/pom.xml
  releng/org.dice.deployments.update.ide/pom.xml
  releng/org.dice.deployments.update.standalone/pom.xml
  releng/pom.xml
"


function usage()
{
  cat <<EOF >&2
$0 - Script for preparing new plugin release

Usage:
  $0 NEW_VERSION LOCATION
    NEW_VERSION should follow semantic versioning scheme without qualifiers.
    LOCATION should be parent location of the update and update_standalone sites.

Examples:
  $0 0.3.1 gh-pages

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

function extract_orbit_repo()
{
  xpath -q -e '/project/properties/orbit-repo.url/text()' $master_pom
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

function build_artifacts()
{
  mvn clean verify
}

function output_artifacts()
{
  local base=$1; shift
  local timestamp=$1; shift
  local orbit_repo=$1; shift

  # XML print (going to hell for this)
  cat <<EOF > $base/compositeArtifacts.xml
<?xml version="1.0" encoding="UTF-8"?>
<?compositeArtifactRepository version="1.0.0"?>
<repository
    name="DICE Deployments"
    type="org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository"
    version="1.0.0">
  <properties size="1">
    <property name="p2.timestamp" value="$timestamp"/>
  </properties>
  <children size="3">
    <child location="ide"/>
    <child location="standalone"/>
    <child location="$orbit_repo"/>
  </children>
</repository>
EOF
}

function output_content()
{
  local base=$1; shift
  local timestamp=$1; shift
  local orbit_repo=$1; shift

  # XML print (going to hell for this)
  cat <<EOF > $base/compositeContent.xml
<?xml version="1.0" encoding="UTF-8"?>
<?compositeMetadataRepository version="1.0.0"?>
<repository
    name="DICE Deployments"
    type="org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository"
    version="1.0.0">
  <properties size="1">
    <property name="p2.timestamp" value="$timestamp"/>
  </properties>
  <children size="3">
    <child location="ide"/>
    <child location="standalone"/>
    <child location="$orbit_repo"/>
  </children>
</repository>
EOF
}

function output_versioned_children()
{
  local base=$1; shift
  local output_file=$1; shift
  local subdirs=$(ls -d $base/*/)
  local subdirs_count=$(ls -dl $base/*/ | wc -l)

  echo "  <children size=\""$subdirs_count"\">" >> $output_file
  for d in $subdirs
  do
    local version=$(basename $d)
    echo "    <child location=\"$version\"/>" >> $output_file
  done
  echo "  </children>" >> $output_file
}

function output_versioned_artifacts()
{
  local base=$1; shift
  local timestamp=$1; shift

  # XML print (going to hell for this)
  cat <<EOF > $base/compositeArtifacts.xml
<?xml version="1.0" encoding="UTF-8"?>
<?compositeArtifactRepository version="1.0.0"?>
<repository
    name="DICE Deployments"
    type="org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository"
    version="1.0.0">
  <properties size="1">
    <property name="p2.timestamp" value="1501573852000"/>
  </properties>
EOF

  output_versioned_children $base $base/compositeArtifacts.xml

  cat <<EOF >> $base/compositeArtifacts.xml
</repository>
EOF
}

function output_versioned_content()
{
  local base=$1; shift
  local timestamp=$1; shift

  # XML print (going to hell for this)
  cat <<EOF > $base/compositeContent.xml
<?xml version="1.0" encoding="UTF-8"?>
<?compositeMetadataRepository version="1.0.0"?>
<repository
    name="DICE Deployments"
    type="org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository"
    version="1.0.0">
  <properties size="1">
    <property name="p2.timestamp" value="1501573852"/>
  </properties>
EOF

  output_versioned_children $base $base/compositeContent.xml

  cat <<EOF >> $base/compositeContent.xml
</repository>
EOF
}


function prepare_update_site()
{
  local location="${1%/}/$2"; shift
  local timestamp="$(date '+%s')000"  # seconds -> miliseconds using concat
  local orbit_repo="$(extract_orbit_repo)"

  mkdir -p $location
  output_artifacts $location $timestamp $orbit_repo
  output_content $location $timestamp $orbit_repo

  for part in ide standalone
  do
    cp -rv releng/org.dice.deployments.update.$part/target/repository \
      $location/$part
  done
}

function split_off_ide_site()
{
  local location_standalone=$1; shift
  local location_ide=$1; shift
  local new_version=$1; shift

  mkdir -p $location_ide/$new_version
  cp -r $location_standalone/$new_version/ide/* $location_ide/$new_version

  output_versioned_content $location_ide
  output_versioned_artifacts $location_ide
}

function main()
{
  local old_version=$(extract_current_version)
  local new_version=$1; shift
  local location=$1; shift

  local location_standalone="$location/updates_standalone"
  local location_ide="$location/updates"

  replace_osgi_version $old_version $new_version
  replace_mvn_version $old_version $new_version

  build_artifacts

  prepare_update_site $location_standalone $new_version
  output_versioned_content $location_standalone
  output_versioned_artifacts $location_standalone

  split_off_ide_site "$location_standalone" "$location_ide" $new_version
}

[[ -z "$1" ]] && usage "Missing version argument"
validate_version $1 || usage "Invalid version $1"

[[ -z "$2" ]] && usage "Missing location argument"
[[ -d "$2" ]] || usage "Missing location '$2'"
[[ -d "${2%/}/updates_standalone/$1" ]] && usage "Folder for standalone version $1 already exists"
[[ -d "${2%/}/updates/$1" ]] && usage "Folder for ide version $1 already exists"

main $1 $2
