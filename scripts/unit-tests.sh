#!/bin/bash
# This is to be used for running unit-tests tests.

############################################################
# Run                                                      #
############################################################
Run()
{
  if ! ./gradlew clean assembleDebug -PdisablePreDex --stacktrace;
  then
    exit 1
  fi

  if ! ./gradlew androidApp:testDebugUnitTest;
  then
    exit 1
  fi

  if ! ./gradlew shared:testDebugUnitTest;
  then
    exit 1
  fi
}

############################################################
############################################################
# Main program                                             #
############################################################
############################################################
Run

