#!/bin/bash

touch secret.properties

{
  echo "PODCAST_INDEX_KEY=\"$PODCAST_INDEX_KEY\""
  echo "PODCAST_INDEX_SECRET=\"$PODCAST_INDEX_SECRET\""
} >> secret.properties
