#!/usr/bin/env bash

# Runs one bounded Java-modernisation assessment. It intentionally allows only
# one outstanding candidate: the presence of codex/modernisation on origin is
# back pressure and makes this invocation stop without changes.

# This runner has no local concurrency protection. Do not schedule overlapping
# invocations; concurrent runs may both perform assessment work before the
# remote codex/modernisation branch provides back pressure.

# Exit on command failures or unset variables, and treat a failure anywhere in a
# pipeline as a failure of the whole pipeline.
set -euo pipefail

# Optional runner configuration: export any CODEX_MODERNISATION_* variable
# before starting this script, or prefix the invocation with it, for example
# CODEX_MODERNISATION_BASE_BRANCH=release ./run-scheduled-java-modernisation.sh.
# Unset variables use the defaults below. Tool configuration, credentials, and
# PATH remain inherited runtime dependencies rather than runner parameters.
branch_name="${CODEX_MODERNISATION_BRANCH_NAME:-codex/modernisation}"
base_branch="${CODEX_MODERNISATION_BASE_BRANCH:-main}"
source_repository="${CODEX_MODERNISATION_SOURCE_REPOSITORY:-https://github.com/albertattard/demo-supermarket-starter.git}"

# A scheduled run has no terminal user to dismiss a pager. Keep command output
# flowing to the log and terminal rather than waiting for `q` in `less`.
export PAGER=cat
export GIT_PAGER=cat
export GH_PAGER=cat

# Report an unrecoverable error to stderr and terminate with failure.
fail() {
  printf 'Error: %s\n' "$*" >&2
  exit 1
}

# Report an expected no-op outcome to stdout and terminate successfully.
stop() {
  printf '%s\n' "$*"
  exit 0
}

# The model is instructed not to alter Git metadata, but that instruction is
# not a security boundary. Confirm the selected repository identity, branch,
# commit, and origin URLs at each boundary where the runner will make a
# GitHub-visible decision.
assert_repository_integrity() {
  local expected_head="$1"
  local current_branch
  local current_head
  local current_branch_head
  local current_origin_fetch_urls
  local current_origin_push_urls

  current_branch="$(git branch --show-current)"
  current_head="$(git rev-parse HEAD)"
  current_branch_head="$(git rev-parse --verify "$branch_name^{commit}")"
  current_origin_fetch_urls="$(git remote get-url --all origin)"
  current_origin_push_urls="$(git remote get-url --push --all origin)"

  [[ "$current_branch" == "$branch_name" ]] \
    || fail "The current branch changed unexpectedly: $current_branch"
  [[ "$current_head" == "$expected_head" ]] \
    || fail 'HEAD changed unexpectedly. Refusing to publish the candidate.'
  [[ "$current_branch_head" == "$expected_head" ]] \
    || fail "Branch $branch_name no longer points at the expected commit."
  [[ "$current_origin_fetch_urls" == "$origin_fetch_urls" ]] \
    || fail 'The origin fetch URL changed unexpectedly. Refusing to publish the candidate.'
  [[ "$current_origin_push_urls" == "$origin_push_urls" ]] \
    || fail 'The origin push URL changed unexpectedly. Refusing to publish the candidate.'
}

# Maven executes build configuration and the wrapper as code. The agent may
# modernise application code and tests, but it must not modify Maven's build
# definition or wrapper before the runner performs its authoritative build.
assert_maven_definition_unchanged() {
  local changed_files

  # Staged changes were rejected earlier. Collect every unstaged, untracked, or
  # ignored Maven build input in stable, duplicate-free order before Maven is
  # allowed to run.
  changed_files="$(
    {
      git diff --name-only -- \
        ':(glob)**/pom.xml' \
        .mvn \
        mvnw \
        mvnw.cmd
      git ls-files --others --exclude-standard -- \
        ':(glob)**/pom.xml' \
        .mvn \
        mvnw \
        mvnw.cmd
      git ls-files --others --ignored --exclude-standard -- \
        ':(glob)**/pom.xml' \
        .mvn \
        mvnw \
        mvnw.cmd
    } | LC_ALL=C sort -u
  )"

  [[ -z "$changed_files" ]] \
    || fail "The agent modified Maven build files. Refusing to run Maven or publish: $changed_files"
}

# Fail before creating the temporary checkout unless every required host tool is
# available. Git manages the candidate; GitHub CLI publishes the draft PR; jq
# validates the agent hand-off.
command -v codex >/dev/null 2>&1 || fail 'Codex CLI is required.'
command -v git >/dev/null 2>&1 || fail 'git is required.'
command -v gh >/dev/null 2>&1 || fail 'GitHub CLI (gh) is required.'
command -v jq >/dev/null 2>&1 || fail 'jq is required.'

# Each scheduled assessment uses an isolated temporary workspace. Its repository
# checkout and runner-owned evidence are sibling directories, so evidence cannot
# appear in the candidate diff.
working_directory="$(mktemp -d "${TMPDIR:-/tmp}/demo-supermarket-modernisation.XXXXXX")" \
  || fail 'Could not create a temporary working directory.'

# Clone the requested base into its dedicated workspace directory, then make it
# the current directory so every following Git operation targets that checkout.
repository_directory="$working_directory/repository"
git clone --quiet --branch "$base_branch" --single-branch "$source_repository" "$repository_directory" \
  || fail "Could not clone $source_repository."
cd "$repository_directory"

# Refresh the exact remote-tracking ref used below. A named fetch may otherwise
# update only FETCH_HEAD, leaving origin/$base_branch at the clone-time commit.
# Then stop if origin already holds an outstanding candidate awaiting review.
git fetch --quiet origin "+refs/heads/$base_branch:refs/remotes/origin/$base_branch"
if git ls-remote --exit-code --heads origin "refs/heads/$branch_name" >/dev/null 2>&1; then
  stop "Remote branch $branch_name already exists; it is the outstanding review candidate."
fi
base_commit="$(git rev-parse --verify "refs/remotes/origin/$base_branch^{commit}")" \
  || fail "Could not resolve origin/$base_branch."
origin_fetch_urls="$(git remote get-url --all origin)" \
  || fail 'Could not resolve the origin fetch URL.'
origin_push_urls="$(git remote get-url --push --all origin)" \
  || fail 'Could not resolve the origin push URL.'

# Create the runner-owned evidence directory, then reserve paths for the agent
# hand-off, Codex execution log, and generated draft-PR description.
evidence_dir="$working_directory/evidence"
mkdir "$evidence_dir" || fail 'Could not create the evidence directory.'
handoff_file="$evidence_dir/handoff.json"
agent_log_file="$evidence_dir/agent.log"
pr_body_file="$evidence_dir/pull-request.md"

# The script, rather than the agent, makes the Git-state decision. If the
# agent concludes that no change is justified, the runner stops without
# committing, pushing, or opening a pull request.
git switch --create "$branch_name" --track "origin/$base_branch"

# Confirm the runner started from the fetched base on the intended branch and
# remote.
assert_repository_integrity "$base_commit"

# This is the workflow’s only model-driven step. Ideally run this runner on a
# separate VM with only the tools required below, so Codex cannot access a
# developer workstation or unrelated credentials. Validation and publication
# remain runner-controlled host operations below.
codex exec \
  --ephemeral \
  --sandbox workspace-write \
  --output-last-message "$handoff_file" \
  - <<'EOF' > "$agent_log_file" 2>&1
Goal:
Identify at most one worthwhile Java 25 modernisation opportunity and implement it as a focused, uncommitted change on the already-prepared branch.

Constraints:
- Keep the project on Java 25. Do not alter the runtime or toolchain baseline.
- Consider unnecessarily Java-8-era code, such as mutable value objects, boilerplate equals, hashCode, or toString, manual collection processing, avoidable nullable control flow, and verbose conditional logic.
- Choose one cohesive opportunity only. Do not perform a broad refactor or mix unrelated cleanup into the change.
- Preserve observable behaviour, public routes, persistence behaviour, and test intent.
- Modernise only where a Java 25 idiom materially improves clarity, safety, or maintainability. Do not make changes for novelty.
- Do not overwrite, revert, stage, or commit unrelated existing changes.
- Do not create or switch branches. Do not stage, commit, push, or create a pull request.
- Do not write the hand-off into the repository. Your final response is captured separately by the caller.

Process:
1. Inspect the codebase and relevant tests. Select the strongest single candidate, if one exists.
2. If no candidate provides a material benefit, do not change source files. Return the no-improvement hand-off below.
3. Otherwise, implement only the selected change. Add or adjust tests where necessary to demonstrate that behaviour remains correct.
4. Run ./mvnw test. Resolve failures caused by the change.
5. Review the final unstaged diff for scope and correctness.

Final response:
Output only valid JSON: no Markdown fences, explanation, or surrounding text.

If no improvement is justified:

{"outcome":"no-improvement","summary":"<why no candidate justified a change>"}

If you implemented a modernisation and ./mvnw test passed:

{"outcome":"modernised","title":"<concise pull-request title>","commit_message":"<imperative commit subject>","summary":"<what changed and why it is worthwhile>","files_changed":["<repository-relative path>"],"validation":[{"command":"./mvnw test","result":"passed"}],"review_focus":"<compatibility risks or review considerations>"}
EOF

# Reject unexpected branch, commit, or remote changes before interpreting the
# hand-off or running repository-controlled build code.
assert_repository_integrity "$base_commit"

# Reject a missing, malformed, or unsupported agent hand-off before making any
# Git or publication decision based on it.
jq -e 'type == "object" and (.outcome == "no-improvement" or .outcome == "modernised")' "$handoff_file" >/dev/null \
  || fail "The agent hand-off is missing or invalid: $handoff_file"

# A no-improvement result must leave no tracked or non-ignored untracked
# changes in the disposable checkout. Verify that claim, then stop without
# committing, pushing, or opening a PR.
outcome="$(jq -er '.outcome' "$handoff_file")" \
  || fail "Could not read the agent outcome: $handoff_file"
if [[ "$outcome" == 'no-improvement' ]]; then
  if [[ -n "$(git status --porcelain)" ]]; then
    fail "The agent reported no improvement but changed the repository. Inspect $evidence_dir before continuing."
  fi
  stop "No worthwhile modernisation was found. Evidence: $handoff_file"
fi

# A modernised result needs complete, minimally valid metadata before the
# runner can verify the actual diff, create a commit, and prepare a draft PR.
jq -e '
  (.title | type == "string" and length > 0) and
  (.commit_message | type == "string" and length > 0 and contains("\n") | not) and
  (.summary | type == "string" and length > 0) and
  (.files_changed | type == "array" and length > 0 and all(.[]; type == "string" and length > 0)) and
  (.validation | type == "array" and length > 0) and
  (.review_focus | type == "string" and length > 0)
' "$handoff_file" >/dev/null || fail "The modernised hand-off is incomplete: $handoff_file"

# Codex may change files but must not stage them; only this deterministic
# runner may choose the verified files to add to the commit.
if ! git diff --cached --quiet; then
  fail "The agent staged changes. Inspect $evidence_dir and the Git index before continuing."
fi

# Do not execute a Maven wrapper or build definition that the agent changed.
assert_maven_definition_unchanged

# The runner repeats the full verification rather than treating the model’s
# report of its focused test run as sufficient evidence for publication.
./mvnw clean verify

# Confirm the branch, HEAD, and remote URLs still match the fetched base before
# the runner identifies and stages the candidate diff. The candidate commit and
# its parent are checked separately immediately before push.
assert_repository_integrity "$base_commit"

# Independently record every modified or non-ignored untracked file, in stable
# order. A modernisation claim without an actual diff is invalid and must not
# be published.
actual_files_file="$evidence_dir/actual-files.txt"
{
  git diff --name-only --no-renames
  git ls-files --others --exclude-standard
} | LC_ALL=C sort -u > "$actual_files_file"
if [[ ! -s "$actual_files_file" ]]; then
  fail "The agent reported a modernisation but produced no changes. Inspect $evidence_dir."
fi

# Require the agent’s reported file list to exactly match the independently
# observed change set before staging or publishing the candidate.
reported_files_file="$evidence_dir/reported-files.txt"
jq --raw-output '.files_changed[]' "$handoff_file" | LC_ALL=C sort > "$reported_files_file"
cmp --silent "$actual_files_file" "$reported_files_file" \
  || fail "The hand-off file list does not match the actual diff. Inspect $evidence_dir."

# Stage only the independently observed files, reject whitespace errors, then
# create the candidate commit using the validated hand-off’s commit subject.
while IFS= read -r file; do
  git add -- "$file"
done < "$actual_files_file"
git diff --cached --check
git commit --message "$(jq --raw-output '.commit_message' "$handoff_file")"

# Confirm the candidate is exactly one commit on the fetched base, then use its
# commit ID as the expected state for the final pre-push integrity check.
candidate_commit="$(git rev-parse HEAD)"
[[ "$(git rev-parse HEAD^)" == "$base_commit" ]] \
  || fail 'The candidate commit does not have the expected base commit.'
assert_repository_integrity "$candidate_commit"

# A remote branch is also the cross-machine queue marker. If this push loses a
# race, it fails and leaves the local candidate for a human to inspect.
git push --set-upstream origin "$branch_name"

# Build the draft PR description from the validated hand-off and independently
# observed change set, recording the runner's successful full verification and
# preserving human review and merge as separate decisions.
{
  printf '%s\n\n' '## Summary'
  jq --raw-output '.summary' "$handoff_file"
  printf '\n%s\n' '## Files changed'
  sed 's/^/- `/' "$actual_files_file" | sed 's/$/`/'
  printf '\n%s\n' '## Validation'
  jq --raw-output '.validation[] | "- `\(.command)`: \(.result)"' "$handoff_file"
  printf '%s\n' '- `./mvnw clean verify`: passed (scheduled runner)'
  printf '\n%s\n\n' '## Review focus'
  jq --raw-output '.review_focus' "$handoff_file"
  printf '%s\n' 'This pull request was created by a scheduled workflow. Human review and an explicit merge decision are required.'
} > "$pr_body_file"

# Publish the verified candidate as a draft PR; it remains unmerged until a
# human reviewer accepts the change and explicitly decides to merge it.
gh pr create \
  --base "$base_branch" \
  --head "$branch_name" \
  --title "$(jq --raw-output '.title' "$handoff_file")" \
  --body-file "$pr_body_file" \
  --draft

printf 'Created a draft pull request. Evidence: %s\n' "$evidence_dir"
