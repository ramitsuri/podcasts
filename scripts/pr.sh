#!/bin/bash
############################################################
# Constants                                                #
############################################################
REMOTE="origin"
MAIN="main"
RESTRICTED_BRANCHES=("main" "develop")

CREATE="Create"
UPDATE="Update"

############################################################
# Variables                                                #
############################################################
MainBranch="main"
PRMode=$UPDATE
KeepCurrentBranchAfterPR=false

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
# Checkout MainBranch and delete current branch after PR   #
############################################################
CheckoutMainBranchDeleteCurrent()
{
   current=$(git branch --show-current)

   if [ "$current" == $MAIN ]
      then
        return
   elif [ "$current" == $MainBranch ]
      then
        return
   elif [ $KeepCurrentBranchAfterPR = true ]
      then
        return
   fi

   echo "CheckoutMainBranchDeleteCurrent Start"

   if ! git checkout $MainBranch;
      then
        exit $?
   fi

   if ! git branch -D "$current";
      then
        exit $?
   fi

   echo "CheckoutMainBranchDeleteCurrent End"
}

############################################################
# Checkout branch for PR number                            #
############################################################
CheckoutBranchForPr()
{
   if [ "$#" -ne 1 ]
      then
        echo "PR number not supplied"
        exit
   fi
   prNumber=$1

   branch=$(gh pr view "$prNumber" --json headRefName --template '{{ .headRefName }}')
   if ! git checkout -b "$branch" "${REMOTE}/${branch}";
      then
        exit $?
   fi

   if ! git pull;
      then
        exit $?
   fi
}

############################################################
# Check if current branch is restricted                    #
############################################################
CheckCurrentBranchRestricted()
{
   current=$(git branch --show-current)

   for restricted_branch in "${RESTRICTED_BRANCHES[@]}"
   do
      if [ "$current" == "$restricted_branch" ] ; then
         echo "${current} is a restricted branch. Submit changes on a different branch"
         exit 1
      fi
   done
}

############################################################
# Run                                                     #
############################################################
Run()
{
   CheckCurrentBranchRestricted

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

   CheckoutMainBranchDeleteCurrent
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