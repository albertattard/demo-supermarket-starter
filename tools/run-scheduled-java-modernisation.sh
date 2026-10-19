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
umask 077

# Optional runner configuration: export any CODEX_MODERNISATION_* variable
# before starting this script, or prefix the invocation with it, for example
# CODEX_MODERNISATION_BASE_BRANCH=release ./run-scheduled-java-modernisation.sh.
# The controller needs Git/GitHub credentials; Codex authentication belongs to
# the VM's agent account. Never forward controller credentials or the SSH agent.
# Required: CODEX_MODERNISATION_WORKER_HOST (the oci-worker public_ip output).
# Optional: CODEX_MODERNISATION_SSH_PRIVATE_KEY_PATH; otherwise use local SSH
# configuration or ssh-agent. SSH always connects as opc, then switches to agent.
branch_name="${CODEX_MODERNISATION_BRANCH_NAME:-codex/modernisation}"
base_branch="${CODEX_MODERNISATION_BASE_BRANCH:-main}"
source_repository="${CODEX_MODERNISATION_SOURCE_REPOSITORY:-https://github.com/albertattard/demo-supermarket-starter.git}"
worker_host="${CODEX_MODERNISATION_WORKER_HOST:-}"
ssh_options=(-o BatchMode=yes -o ConnectTimeout=10 -o ConnectionAttempts=1
  -o StrictHostKeyChecking=accept-new -o ForwardAgent=no -o ClearAllForwardings=yes
  -o ServerAliveInterval=15 -o ServerAliveCountMax=3)
ssh_private_key_path="${CODEX_MODERNISATION_SSH_PRIVATE_KEY_PATH:-}"
if [[ -n "$ssh_private_key_path" ]]; then
  ssh_options+=(-i "$ssh_private_key_path" -o IdentitiesOnly=yes)
fi

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

# Require complete publication metadata before verification or publication.
assert_modernised_handoff() {
  local handoff_file="$1"
  jq -e '
    (.title | type == "string" and length > 0) and
    (.commit_message | type == "string" and length > 0 and contains("\n") | not) and
    (.summary | type == "string" and length > 0) and
    (.files_changed | type == "array" and length > 0 and all(.[]; type == "string" and length > 0)) and
    (.validation | type == "array" and length > 0) and
    (.review_focus | type == "string" and length > 0)
  ' "$handoff_file" >/dev/null || fail "The modernised hand-off is incomplete: $handoff_file"
}

# Resolve the shared prompt beside this script before changing directories.
script_directory="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
prompt_source="$script_directory/java-modernisation-prompt.md"
[[ -s "$prompt_source" && -r "$prompt_source" ]] \
  || fail "The modernisation prompt is missing, empty, or unreadable: $prompt_source"

# Fail before cloning unless the controller has its required tools and worker
# address. The controller needs Java 25 for its independent Maven build.
[[ "$worker_host" =~ ^[a-zA-Z0-9][a-zA-Z0-9.-]*$ ]] \
  || fail 'Set CODEX_MODERNISATION_WORKER_HOST to the OCI worker IPv4 address or hostname.'
command -v ssh >/dev/null 2>&1 || fail 'OpenSSH client is required.'
worker_ssh() {
  ssh "${ssh_options[@]}" "opc@$worker_host" "$@"
}
command -v git >/dev/null 2>&1 || fail 'git is required.'
command -v gh >/dev/null 2>&1 || fail 'GitHub CLI (gh) is required.'
command -v jq >/dev/null 2>&1 || fail 'jq is required.'
command -v java >/dev/null 2>&1 || fail 'Java 25 is required on the controller.'
command -v javac >/dev/null 2>&1 || fail 'JDK 25 is required on the controller.'

# Each scheduled assessment uses an isolated temporary workspace. Its repository
# checkout and runner-owned evidence are sibling directories, so evidence cannot
# appear in the candidate diff.
working_directory="$(mktemp -d "${TMPDIR:-/tmp}/demo-supermarket-modernisation.XXXXXX")" \
  || fail 'Could not create a temporary working directory.'

# Keep each invocation in its own directory; retain files for demo inspection.
repository_directory="$working_directory/repository"
evidence_dir="$working_directory/evidence"
mkdir "$evidence_dir"
# Preserve and send the exact prompt used, independently of agent changes.
cp "$prompt_source" "$evidence_dir/prompt.md"
worker_script="$working_directory/worker.sh"

# Clone the requested base into its dedicated workspace directory, then make it
# the current directory so every following Git operation targets that checkout.
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

# Reserve evidence paths for the agent hand-off, Codex execution log, and
# generated draft-PR description. The directory was created before cloning.
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

# Send repository history as a bundle, without controller Git configuration,
# credentials, hooks, or a forwarded SSH agent. Each run gets its own VM workspace.
worker_directory="$(worker_ssh 'sudo -n -iu agent mktemp -d /home/agent/work/modernisation.XXXXXX')"
[[ "$worker_directory" =~ ^/home/agent/work/modernisation\.[a-zA-Z0-9]+$ ]] \
  || fail 'The worker returned an invalid workspace path.'
printf 'Controller evidence: %s\nWorker evidence: %s:%s\n' \
  "$evidence_dir" "$worker_host" "$worker_directory"
git bundle create "$working_directory/input.bundle" "$branch_name"
worker_ssh "sudo -n -iu agent tee '$worker_directory/input.bundle' >/dev/null" \
  < "$working_directory/input.bundle"

worker_ssh "sudo -n -iu agent tee '$worker_directory/prompt.md' >/dev/null" \
  < "$evidence_dir/prompt.md"

# Build the worker script for checkout preparation, Codex, and result export.
# The authoritative verification and publication stay on the controller.
{
  printf '%s\n' '#!/usr/bin/env bash' 'set -euo pipefail' 'umask 077'
  printf 'branch_name=%q\n' "$branch_name"
  cat <<'WORKER'
# sudo -iu agent supplies JAVA_HOME and PATH through the worker's login profile.
# Send output directly to logs so interactive pagers cannot block this unattended run.
export PAGER=cat GIT_PAGER=cat GH_PAGER=cat

# Require the unprivileged agent account so Codex cannot inherit opc or root privileges.
[[ "$(id -un)" == agent ]] || { printf '%s\n' 'Worker must run as agent.' >&2; exit 1; }

# Stop if bootstrap has not completed successfully; required tools or sandbox setup may be missing.
[[ -f /var/lib/java-worker/ready && ! -f /var/lib/java-worker/failed ]] \
  || { printf '%s\n' 'Worker bootstrap is not ready.' >&2; exit 1; }

# Fail before starting the task if Codex is unavailable in the worker login environment.
command -v codex >/dev/null

# Confirm the worker uses JDK 25 so any permitted tests match the project Java baseline.
"$JAVA_HOME/bin/javac" -version 2>&1 | grep -E '^javac 25([.]|$)' >/dev/null

# Enter the workspace containing the bundle uploaded by the controller, clone
# its prepared branch with Git history, and work from that checkout. This gives
# Codex the repository files without requiring GitHub access or credentials.
cd "$1"
git clone --quiet --branch "$branch_name" input.bundle repository
cd repository

# Populate the agent account's Maven distribution and dependency caches before
# entering Codex's network-restricted sandbox. Run tests on the unchanged base
# as well; a preparation failure stops the run before any agent edits.
# Output is captured in the controller's worker.log.
./mvnw --batch-mode --no-transfer-progress clean package
[[ -z "$(git status --porcelain)" ]] \
  || { printf '%s\n' 'Maven preparation changed the checkout; refusing to start Codex.' >&2; exit 1; }

codex exec \
  --ephemeral \
  --sandbox workspace-write \
  --model 'gpt-6.1-sol' \
  --config 'model_reasoning_effort="medium"' \
  --output-last-message "$1/handoff.json" \
  - < "$1/prompt.md" > "$1/agent.log" 2>&1

# Record the worker's Git state, then package all the agent's file changes into
# one patch. The controller downloads both, checks the state, applies the patch
# to its own checkout, and runs clean verify there. Record whether the agent
# staged changes before the patch preparation below touches the Git index.
staged_changes=false
git diff --cached --quiet || staged_changes=true
jq -n \
  --arg branch "$(git branch --show-current)" \
  --arg head "$(git rev-parse HEAD)" \
  --arg origin_fetch_urls "$(git remote get-url --all origin)" \
  --arg origin_push_urls "$(git remote get-url --push --all origin)" \
  --argjson staged_changes "$staged_changes" \
  '{branch: $branch, head: $head, origin_fetch_urls: $origin_fetch_urls,
    origin_push_urls: $origin_push_urls, staged_changes: $staged_changes}' > "$1/repository-state.json"
# Include tracked changes and new files (including binary content) in the patch.
while IFS= read -r -d '' file; do
  git add --intent-to-add -- "$file"
done < <(git ls-files --others --exclude-standard -z)
git diff --binary --no-ext-diff --no-textconv HEAD > "$1/candidate.patch"
WORKER
} > "$worker_script"

# Keep failed workspaces and logs on the VM for diagnosis. An SSH, Codex, or
# execution failure stops publication. Retrieve the patch and evidence for
# independent validation and a build on the controller.
worker_ssh "sudo -n -iu agent bash -s -- '$worker_directory'" \
  < "$worker_script" > "$evidence_dir/worker.log" 2>&1 \
  || fail "Worker execution failed. Inspect $evidence_dir/worker.log and $worker_host:$worker_directory."
worker_ssh "sudo -n -iu agent cat '$worker_directory/handoff.json'"          > "$handoff_file"
worker_ssh "sudo -n -iu agent cat '$worker_directory/agent.log'"             > "$agent_log_file"
worker_ssh "sudo -n -iu agent cat '$worker_directory/candidate.patch'"       > "$evidence_dir/candidate.patch"
worker_ssh "sudo -n -iu agent cat '$worker_directory/repository-state.json'" > "$evidence_dir/repository-state.json"

# The patch alone cannot reveal staged files or changed worker Git metadata.
# Validate the exported observations on the controller before applying it.
jq -e \
  --arg branch "$branch_name" \
  --arg head "$base_commit" \
  --arg origin "$worker_directory/input.bundle" '
  .branch == $branch
    and .head == $head
    and .origin_fetch_urls == $origin
    and .origin_push_urls == $origin
    and .staged_changes == false
' "$evidence_dir/repository-state.json" >/dev/null \
  || fail "The worker repository state changed unexpectedly. Inspect $evidence_dir."
if [[ -s "$evidence_dir/candidate.patch" ]]; then
  git apply --check "$evidence_dir/candidate.patch"
  git apply "$evidence_dir/candidate.patch"
fi

# Reject unexpected branch, commit, or remote changes before interpreting the
# hand-off or running repository-controlled build code.
assert_repository_integrity "$base_commit"

# Reject a missing, malformed, or unsupported agent hand-off before making any
# Git or publication decision based on it.
jq -e 'type == "object" and (.outcome == "no-improvement" or .outcome == "blocked" or .outcome == "modernised")' "$handoff_file" >/dev/null \
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

# A blocked assessment requires engineer action. Retain any partial changes
# and evidence for inspection, and exit with failure so unattended callers can
# distinguish an actionable blockage from a successful no-improvement run.
if [[ "$outcome" == 'blocked' ]]; then
  jq -e '
    (.summary | type == "string" and test("\\S")) and
    (.required_action | type == "string" and test("\\S"))
  ' "$handoff_file" >/dev/null \
    || fail "The blocked hand-off is incomplete: $handoff_file"
  fail "Modernisation blocked: $(jq -r '.summary' "$handoff_file")
Required action: $(jq -r '.required_action' "$handoff_file")
Evidence: $evidence_dir"
fi

# A modernised result needs complete, minimally valid metadata before the
# runner can verify the actual diff, create a commit, and prepare a draft PR.
assert_modernised_handoff "$handoff_file"

# Codex may change files but must not stage them; only this deterministic
# runner may choose the verified files to add to the commit.
if ! git diff --cached --quiet; then
  fail "The agent staged changes. Inspect $evidence_dir and the Git index before continuing."
fi

# Do not execute a Maven wrapper or build definition that the agent changed.
assert_maven_definition_unchanged

# Independently build the returned change on the controller. Worker test reports
# are evidence only and never replace this publication gate.
./mvnw clean verify
assert_maven_definition_unchanged
git diff --cached --quiet || fail 'The controller build staged changes.'

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
  printf '%s\n' '- `./mvnw clean verify`: passed (controller, scheduled runner)'
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
