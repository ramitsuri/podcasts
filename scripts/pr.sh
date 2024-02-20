#!/bin/bash
############################################################
# Constants                                                #
############################################################
REMOTE="origin"

CREATE="Create"
UPDATE="Update"

############################################################
# Variables                                                #
############################################################
MainBranch="main"
PRMode=$UPDATE

############################################################
# SquashCommits                                            #
############################################################
SquashCommits()
{
   current=$(git branch --show-current)
   if [ "$#" -ne 1 ]
   then
     against="$REMOTE/$current"
   else
     against=$MainBranch
   fi

   echo "SquashCommits Start against $against"
   echo "Squashing all commits from $current"
   if ! git reset "$(git merge-base $against "$current")";
   then
     exit $?
   fi
   echo "SquashCommits End"
}

############################################################
# Commit                                                   #
############################################################
Commit()
{
   echo "Commit Start"
   git add -A
   if ! git commit;
   then
     exit $?
   fi
   echo "Commit End"
}

############################################################
# Push                                                     #
############################################################
Push()
{
   echo "Push Start"
   branch=$(git branch --show-current)
   if ! git push -u $REMOTE "$branch";
   then
     exit $?
   fi
   echo "Push End"
}

############################################################
# Create PR                                                #
############################################################
CreatePR()
{
   echo "CreatePR Start"
   if ! gh pr create --fill --base $MainBranch;
   then
     exit $?
   fi
   echo "CreatePR End"
}

############################################################
# Run                                                     #
############################################################
Run()
{
   if [ $PRMode == $CREATE ]
   then
     SquashCommits $MainBranch
   else
     SquashCommits
   fi

   Commit

   Push

   if [ $PRMode == $CREATE ]
   then
     CreatePR
   fi
}

############################################################
############################################################
# Main program                                             #
############################################################
############################################################
while getopts "cub:" option; do
   case $option in
      c) # Create PR
         PRMode=$CREATE;;
      u) # Update PR
         PRMode=$UPDATE;;
      b) # Set main branch commits
         MainBranch=$OPTARG;;
      \?) # Invalid option
         echo "Error: Invalid option"
         exit;;
   esac
done

Run

############################################################
# SetMode  (Not used)                                      #
############################################################
SetMode()
{
   mode=$1
   if [ "$mode" == "C" ]
   then
      PRMode=$CREATE
   elif [ "$mode" == "U" ]
   then
      PRMode=$UPDATE
   else
      echo "Invalid PR mode. Should be C (for create) or U (for update)"
      exit
   fi
}