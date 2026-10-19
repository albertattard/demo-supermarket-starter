---
name: review-implementation
description: Independently review one implementation candidate in the current repository against its GitHub issue and evidence matrix. Use when a completed candidate needs a read-only Staff Software Engineer review before eligibility, repair, or selection.
---

# Review Implementation

## Establish the review contract

1. Act as a Staff Software Engineer conducting an independent, read-only review.
2. Read the issue, applicable repository instructions, supplied exact baseline commit, canonical requirements-to-evidence matrix, explicit exclusions, and verification commands before inspecting the candidate.
3. Confirm the assigned worktree and baseline. Review all changes from the supplied baseline to the candidate's `HEAD`, plus uncommitted changes. If the baseline, issue, matrix, worktree, or required repository access is missing or ambiguous, return `BLOCKED` and identify exactly what is needed.
4. Treat the issue and canonical matrix as the contract. Do not infer a different objective from the review request, implementation summary, or repository preferences.
5. Work read-only. Do not modify, stage, commit, reset, switch branches, create worktrees, or run destructive commands.

## Inspect independent evidence

1. Inspect the actual candidate diff, changed tests, and relevant surrounding production code. Run the supplied verification commands only within the resource reservation defined by the orchestrator.
2. Assess correctness, architecture, security, performance, maintainability, test coverage, and production readiness. Ignore minor style or formatting concerns unless they conceal a material defect.
3. Audit each in-scope matrix row. Check that the named test actually proves the stated final result; do not accept a passing lifecycle, a controller-only test, or an implementation claim as proof of a user journey, persistence invariant, migration constraint, or snapshot.
4. Query pending work with `gh issue list --state open`. An open issue suppresses a finding only when it documents the same pre-existing defect and this candidate neither introduces nor materially worsens it. Otherwise report the finding and cite the related issue.
5. Do not inspect another candidate, another reviewer's findings, or the writer's self-assessment. Those inputs anchor the review and are not independent evidence.

## Report one complete result

1. Return exactly one terminal verdict:
   - `ACCEPT` when no material issue or uncovered in-scope matrix row remains;
   - `CHANGES_REQUESTED` when the candidate has one or more material, in-scope defects or evidence gaps; or
   - `BLOCKED` when the review cannot be completed reliably.
2. For `CHANGES_REQUESTED`, report every material finding in one severity-ordered list. For each finding, include affected code or matrix ID, concrete evidence, impact, and a repair recommendation. Do not manufacture findings.
3. For `ACCEPT`, list the verification inspected or run, the matrix rows audited, and any non-blocking risks. Do not claim that acceptance proves the implementation is perfect.
4. For `BLOCKED`, state the unavailable input, command, access, or ambiguous contract and why it prevents a reliable verdict.
5. Do not select a winning candidate or direct merging. The orchestrator determines eligibility and passes a complete `CHANGES_REQUESTED` finding set to the original writer for at most one review-driven repair cycle.
