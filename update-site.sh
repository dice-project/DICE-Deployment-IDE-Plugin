#!/bin/bash

SOURCE=site
TARGET=gh-pages

THINGS_TO_COPY=(
  site.xml
)
THINGS_TO_MOVE=(
  artifacts.jar
  content.jar
  features
  plugins
)

for thing in ${THINGS_TO_COPY[@]}
do
  echo "Copying $thing ..."
  rm -rf $TARGET/$thing
  cp -rf $SOURCE/$thing $TARGET
done

for thing in ${THINGS_TO_MOVE[@]}
do
  echo "Moving $thing ..."
  rm -rf $TARGET/$thing
  mv $SOURCE/$thing $TARGET
done