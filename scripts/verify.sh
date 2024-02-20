#!/bin/bash
# This is to be used with a CI/CD setup to run tasks like lint formatting,
# assembling and running unit tests for the project.
#
# This script also automatically commits and pushes the changes from
# running lint formatting and for that, uses git config defined as
# constants below. Make sure that if running the script locally, it is
# run after you have already committed your real changes otherwise
# they'll be committed with the defined user config.

############################################################
# Constants                                                #
############################################################
GIT_USER_NAME="github-actions"
GIT_USER_EMAIL="github-actions@github.com"

############################################################
# Run                                                      #
############################################################
Run()
{
  if ! ./gradlew ktlintFormat;
  then
    exit 1
  fi

  if ! ./gradlew ktlintCheck;
  then
    exit 1
  fi

  CommitFormattingChanges

  if ! ./scripts/unit-tests.sh;
  then
    exit 1
  fi
}

############################################################
# Commit Formatting Changes                                #
############################################################
CommitFormattingChanges()
{
  if [ -z "$(git status --porcelain)" ] # If file changes empty or null then do nothing
  then
    return
  fi

  username=$(git config user.name)
  email=$(git config user.email)
  git config user.name "$GIT_USER_NAME"
  git config user.email "$GIT_USER_EMAIL"

  if ! git commit --allow-empty -am "Automated lint formatting changes";
  then
    exit 1
  fi

  if ! git push;
  then
    exit 1
  fi

  git config user.name "$username"
  git config user.email "$email"
}

############################################################
############################################################
# Main program                                             #
############################################################
############################################################
Run
