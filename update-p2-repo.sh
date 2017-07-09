#!/bin/bash

# Generate data

function find_folder()
{
  local base=$1; shift
  find $base -maxdepth 1 -mindepth 1 -type d "$@"
}

function no_children()
{
  echo $(find_folder $1 | wc -l)
}

function get_children()
{
  for c in $(find_folder $1 -exec basename {} \; | sort)
  do
    echo "    <child location=\"$c\"/>"
  done
}

function output_artifacts()
{
  local base=$1; shift
  local timestamp=$1; shift
  local size=$1; shift
  local children=$1; shift

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
  <children size="$size">
$children
  </children>
</repository>
EOF
}

function output_content()
{
  local base=$1; shift
  local timestamp=$1; shift
  local size=$1; shift
  local children=$1; shift

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
  <children size="$size">
$children
  </children>
</repository>
EOF
}

function main()
{
  local timestamp="$(date '+%s')000"  # seconds -> miliseconds using concat
  local children=$(get_children $1)
  local size=$(no_children $1)

  output_artifacts $1 $timestamp $size "$children"
  output_content $1 $timestamp $size "$children"
}

[[ -z $1 ]] && echo "Specify update folder" && exit 1
main $1
