#!/bin/bash

# Constants
REMOTE="origin"
RESTRICTED_BRANCHES=("main")
REVIEWERS=()
REPO="ramitsuri/podcasts.git"

# Variables
CreatingPr=false
BaseBranch="main"
RunLintCheck=true
SquashCommits=true
KeepCurrentBranchAfterPr=false
RunTests=false
Draft=false
ReadyFromDraft=false
ExitEarly=false

Help()
{
    echo "
This script will automate most of the PR create/update process for an Android project
Including

- running lint check
- running tests
- squashing commits against BaseBranch var when creating a PR or against the remote branch when updating it
- commit changes. Will ask for commit message
- push changes to remote
- create PR on GitHub. GitHub CLI should be installed and authenticated
- checkout BaseBranch and delete branch from which PR was created

Override for the defaults can be provided via a pr.properties txt file in the project's root directory or via input flags to the script

l    Skip lint checks
s    Skip squashing commits
k    Keep the current branch and don't checkout BaseBranch after PR
b    Set the BaseBranch var to squash commits and create PR against
t    Run tests when creating or updating PR
d    Create PR in draft mode (applies only when creating)
r    Mark draft PR ready (applies only when updating)
x    Exit early, without pushing any changes to the repository. Useful for running tests, formatting

Branch for an existing PR can be checked out by using the input flag p

Example pr.properties file

base_branch=feature-test
squash_commits=false
lint_check=false
keep_current_branch_after_pr=true
tests=true
draft_pr=true

To use
- install GitHub cli
- run 'gh auth login'
- select github.com, select ssh, skip selecting ssh key, login with browser
- [Do If Getting Authentication Error] create personal access token to login
- [Do If Still Getting Authentication Error] create ssh key for GitHub cli, run gh auth login and select the created key this time

Examples
./scripts/pr.sh
Create or update PR

./scripts/pr.sh -s
Create or update PR but skip squashing commits

./scripts/pr.sh -b feature-test
Create PR against feature-test branch

./scripts/pr.sh -k
Create or update PR and keep the PR branch

./scripts/pr.sh -p 999
Checkout the remote branch associated with the PR number 999"
}

LintCheck()
{
    echo "LintCheck Start"
    if ! ./gradlew --continue --no-build-cache ktlintFormat;
    then
        exit $?
    fi
    echo "LintCheck End"
}

SquashCommits()
{
    current=$(git branch --show-current)

    # We don't want to rewrite history if the branch exists on remote, so squash against
    # remote branch, otherwise against the base branch
    if git ls-remote --exit-code --heads git@github.com:"$REPO" refs/heads/"$current"
    then
        against="$REMOTE/$current"
    else
        against=$BaseBranch
    fi

    echo "SquashCommits Start against $against"
    if ! git reset "$(git merge-base "$against" "$current")";
    then
        exit $?
    fi
    echo "SquashCommits End"
}

Commit()
{
    echo "Commit Start"
    git add -A
    if [ -z "$(git status --porcelain)" ]
    then
        echo "Nothing to commit"
        echo "Commit End"
        return
    fi
    if ! git commit;
    then
        exit $?
    fi
    echo "Commit End"
}

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

CreatePr()
{
    echo "CreatePr Start"
    reviewers_command=""
    if [ "$Draft" = true ]
    then
        reviewers_command="$reviewers_command --draft"
    else
        for reviewer in "${REVIEWERS[@]}"
        do
            reviewers_command="$reviewers_command --reviewer $reviewer"
        done
    fi

    title=$(git log -1 --format=%s)
    body=$(git log -1 --format=%b)
    echo -e "$body" | gh pr create -t "$title" --base "$BaseBranch" --assignee @me $reviewers_command --body-file -
    echo "CreatePr End"
}

ReadyPr()
{
    echo "ReadyPr Start"
    if ! gh pr ready;
    then
        exit $?
    fi

    command="gh pr edit"
    for reviewer in "${REVIEWERS[@]}"
    do
       command="$command --add-reviewer $reviewer"
    done

    $command
    echo "ReadyPr End"
}

CheckoutBaseBranchDeleteCurrent()
{
    current=$(git branch --show-current)

    if [ "$current" == "$BaseBranch" ]
    then
        return
    elif ! CheckCurrentBranchRestricted;
    then
        return
    elif [ "$KeepCurrentBranchAfterPr" = true ]
    then
        return
    fi

    echo "CheckoutBaseBranchDeleteCurrent Start"

    if ! git checkout "$BaseBranch";
    then
        exit $?
    fi

    if ! git branch -D "$current";
    then
        exit $?
    fi

    echo "CheckoutBaseBranchDeleteCurrent End"
}

CheckoutBranchForPr()
{
    if [ "$#" -ne 1 ]
    then
        echo "PR number not supplied"
        exit
    fi
    prNumber=$1

    if ! git fetch;
    then
        exit $?
    fi

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

CheckCurrentBranchRestricted()
{
    return_value=0
    current=$(git branch --show-current)

    for restricted_branch in "${RESTRICTED_BRANCHES[@]}"
    do
        if [ "$current" == "$restricted_branch" ] ; then
            return_value=1
        fi
    done

    return "$return_value"
}

Tests()
{
    test_commands=(
        androidApp:testDebugUnitTest
        shared:testDebugUnitTest
        shared:jvmTest
    )

    for c in "${test_commands[@]}"
    do
        if ! ./gradlew "$c";
        then
            exit $?
        fi
    done
}

SetVars()
{
    echo "SetVars Start"

    # Check if PR exists for current branch
    if ! pr_view_output=$(gh pr view --json state,baseRefName --template '{{ .state }};;;{{ .baseRefName }}' 2>&1)
    then
        error_code=$?
        # If output contains "no pull requests" it means we're creating a PR
        if [[ $pr_view_output == *"no pull requests"* ]];
        then
            CreatingPr=true
        # Some other error occurred
        else
            echo "$pr_view_output"
            exit $error_code
        fi
    else
        status_base_branch=(${pr_view_output//;;;/ })
        status=${status_base_branch[0]}
        if [ "$status" == "CLOSED" ] || [ "$status" == "MERGED" ]
        then
            echo "Previous PR for the same branch merged or closed, can create PR"
            CreatingPr=true
        else
            pr_base_branch=${status_base_branch[1]}
            echo "Updating PR, will use base branch of the PR: $pr_base_branch"
            CreatingPr=false
        fi
    fi

    file="./pr.properties"

    function prop {
      if [ ! -f $file ]; then
          echo ''
      else
          grep "${1}" ${file} | cut -d'=' -f2
      fi
    }

    # Override script default with script input, if not provided override with value in properties
    if [ -n "$pr_base_branch" ]
    then
        BaseBranch=$pr_base_branch
    elif [ -n "$Input_BaseBranch" ]
    then
        BaseBranch=$Input_BaseBranch
    elif [ -n "$(prop 'base_branch')" ]
    then
        BaseBranch=$(prop 'base_branch')
    fi

    if [ -n "$Input_RunLintCheck" ]
    then
        RunLintCheck=$Input_RunLintCheck
    elif [ -n "$(prop 'lint_check')" ]
    then
        RunLintCheck=$(prop 'lint_check')
    fi

    if [ -n "$Input_SquashCommits" ]
    then
        SquashCommits=$Input_SquashCommits
    elif [ -n "$(prop 'squash_commits')" ]
    then
        SquashCommits=$(prop 'squash_commits')
    fi

    if [ -n "$Input_KeepCurrentBranchAfterPr" ]
    then
        KeepCurrentBranchAfterPr=$Input_KeepCurrentBranchAfterPr
    elif [ -n "$(prop 'keep_current_branch_after_pr')" ]
    then
        KeepCurrentBranchAfterPr=$(prop 'keep_current_branch_after_pr')
    fi

    if [ -n "$Input_RunTests" ]
    then
        RunTests=$Input_RunTests
    elif [ -n "$(prop 'tests')" ]
    then
        RunTests=$(prop 'tests')
    fi

    if [ -n "$Input_Draft" ]
    then
        Draft=$Input_Draft
    elif [ -n "$(prop 'draft_pr')" ]
    then
        Draft=$(prop 'draft_pr')
    fi

    if [ "$Draft" = true ]
    then
        ReadyFromDraft=false
    fi

    bold=$(tput bold)
    normal=$(tput sgr0)

    if [ "$CreatingPr" = true ]
    then
        if [ "$Draft" = true ]
        then
            creating_pr_text="creating in draft mode"
        else
            creating_pr_text="creating"
        fi
    else
        if [ "$ReadyFromDraft" = true ]
        then
            creating_pr_text="updating and marking as ready"
        else
            creating_pr_text="updating"
        fi
    fi

    echo "
${normal}BaseBranch: ${bold}$BaseBranch
${normal}Creating or Updating PR: ${bold}$creating_pr_text
${normal}LintCheck: ${bold}$RunLintCheck
${normal}Squash: ${bold}$SquashCommits
${normal}KeepCurrentBranchAfterPR: ${bold}$KeepCurrentBranchAfterPr
${normal}RunTests: ${bold}$RunTests
${normal}ExitEarly: ${bold}$ExitEarly

${normal}SetVars End"
}

Run()
{
    SetVars

    if ! CheckCurrentBranchRestricted;
    then
        echo "Submit changes on a non restricted branch"
        exit 1
    fi

    if [ "$RunLintCheck" = true ]
    then
        LintCheck
    fi

    if [ "$RunTests" = true ]
    then
        Tests
    fi

    if [ "$ExitEarly" = true ]
    then
        echo "Exiting early"
        exit
    fi

    if [ "$SquashCommits" = true ]
    then
        SquashCommits
    fi

    Commit

    Push

    if [ "$CreatingPr" = true ]
    then
        CreatePr
    elif [ "$ReadyFromDraft" = true ]
    then
        ReadyPr
    fi

    CheckoutBaseBranchDeleteCurrent
}

# Main program
while getopts "hlsktdrxb:p:" option; do
    case $option in
        h)
            Help
            exit;;

        l)
            Input_RunLintCheck=false;;

        s)
            Input_SquashCommits=false;;

        k)
            Input_KeepCurrentBranchAfterPr=true;;

        t)
            Input_RunTests=true;;

        d)
            Input_Draft=true
            ReadyFromDraft=false;;

        r)
            Input_Draft=false
            ReadyFromDraft=true;;

        x)
            ExitEarly=true;;

        b)
            Input_BaseBranch=$OPTARG;;

        p)
            CheckoutBranchForPr "$OPTARG"
            exit;;

        \?)
            echo "Error: Invalid option"
            exit;;
    esac
done

Run
