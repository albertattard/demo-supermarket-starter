---
name: commit-changes
description: Use this skill when I want you to review the current Git changes and create a focused, coherent commit.
disable-model-invocation: true
---

# Commit behaviour

Act as a careful software engineer responsible for preserving a reviewable Git history. Create commits that represent one coherent, independently understandable unit of delivered value. Treat a commit request as permission to inspect and prepare the proposed commit, not as permission to bypass material concerns or absorb unrelated work.

Do not edit, reformat, generate, delete, or otherwise alter files under review. This skill may inspect Git state, stage selected existing changes, and create a commit, but must not change the working tree. If it finds a typo, incomplete change, defect, or other concern, stop, identify it, and ask the user for direction.

## Before committing

Inspect the repository state and complete relevant diff. Check for accidental or suspicious changes, debug output, generated files, unrelated formatting churn, and prose issues in comments, documentation, user-facing text, and changed strings. Also check whether the diff is internally incomplete or unrelated to the requested work.

If a material concern is found, stop and state the issue, where it appears, and why it matters. Wait for explicit direction before committing. Do not block a commit for purely subjective or trivial stylistic preferences.

## Sensitive-information check

Before staging or committing, inspect every intended changed file for credentials and secret material, including passwords, API keys, access tokens, private keys, connection strings containing credentials, cookies, and `.env`-style values.

Use an available secret scanner against the intended diff or staged changes where possible. Also review changed text for obvious credential assignments and high-entropy token-like values.

If potential sensitive information is found:

- Stop before staging or committing.
- Alert the engineer with the file path, line number, and credential category.
- Do not print the suspected value in the report, commit message, or chat.
- Treat it as a material concern unless the engineer explicitly confirms it is a non-secret fixture or placeholder.
- Ask the engineer whether to remove it, replace it with a safe placeholder, move it to approved secret management, or explicitly document an exception.

Do not rely on `.gitignore` as a secret-control mechanism.

## Decide the commit shape

Create a single commit without another approval only when the changes are clean and form one coherent logical unit: they share a clear purpose and can reasonably be reviewed, reverted, and described together.

If changes contain independently meaningful work, such as an unrelated fix, refactor, documentation change, or formatting chore, do not commit yet. Propose a focused commit sequence in execution order, including the subject and affected files or change groups for each. Wait for the user's explicit approval before creating any split commits. Split by logical intent, not file count; split hunks only when the diff clearly supports it.

## Commit messages and identity

Use a concise imperative subject that describes the user, business, operational, or workshop value delivered by the commit, rather than its implementation mechanism.

Prefer `Enable guests to confirm pickup orders` over `Add order confirmation controller`. Prefer `Record approved task-readiness decisions` over `Update TASK-004`. Use implementation terms only when the commit has no meaningful externally visible outcome, such as a narrowly scoped build or maintenance correction.

Add a body only when the rationale, user impact, or a significant trade-off would help a future reader.

Create the commit using the repository's configured Git author identity and signing configuration. Preserve automatic signing when it is enabled. Do not force signing, bypass it, or invent an identity. If the configured or required signing cannot be completed, stop and ask the user for direction.

## Create and verify the commit

After selecting a coherent commit shape and message, stage only the intended files or hunks. Do not use a broad staging command that includes unrelated, pre-existing, generated, or suspicious changes. Inspect `git diff --cached` and run `git diff --cached --check` before committing.

Create the commit with the selected subject and optional body. Then inspect it with `git show --check --stat HEAD` and inspect `git status --short` to confirm that the new commit contains only the intended change set and that any remaining worktree changes are intentionally uncommitted. If the commit fails or this final check reveals a material concern, stop and report it; do not claim that the commit succeeded.

## Report

After committing, report the commit hash and subject concisely. If stopped, make the finding or proposed split easy to approve or revise.
