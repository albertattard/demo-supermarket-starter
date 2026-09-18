---
name: orchestrate-issue-implementations
description: Coordinate a primary and an alternative implementation of a ready GitHub issue in pre-provisioned Git worktrees. Use when the user wants two comparable approaches, isolated implementation agents, or a comparison of a conventional design with a viable alternative.
---

# Orchestrate Issue Implementations

## Plan the pair

1. Read the issue, applicable repository instructions, and enough relevant code and tests to identify genuine design choices.
2. Treat the primary design as the simplest defensible implementation that follows existing repository conventions.
3. Treat the alternative design as a materially different but viable implementation that exposes a useful trade-off. It may be less preferred, but it must satisfy all acceptance criteria and must not weaken security or essential reliability.
4. If no useful alternative exists, state that clearly and leave the alternative worktree unchanged. Do not manufacture a duplicate or a contrived design.
5. Label both designs with their expected strengths, weaknesses, and learning objective.
6. Before assigning writers, derive one shared requirements-to-evidence matrix. Assign stable IDs to every acceptance criterion and every specific observable, data, migration, security, or integrity requirement in scope. Record explicit exclusions and their follow-up work separately. A requirement is not covered merely because a similar lower-level test exists. Give the same canonical matrix to both writers and reviewers.
7. Audit the matrix against the issue before assigning writers. Every in-scope row must have a credible proof strategy: user journey, HTTP/access, domain, persistence, migration, or integration; starting state and action; and final result asserted. Do not assign writers until the matrix has no missing scope row. Never treat an explicit exclusion as a candidate defect.

## Locate prepared worktrees

1. Extract the issue number from the issue URL.
2. Derive the repository name from the `origin` remote URL. Fall back to the current repository directory name only when no `origin` remote is configured.
3. Expect sibling worktrees named `../repository-worktree-issue-<number>-primary` and `../repository-worktree-issue-<number>-alternative`.
4. Confirm both worktrees exist, are clean, are on branches `issue-<number>-primary` and `issue-<number>-alternative`, and point to the same commit as the current repository.
5. If either worktree is missing or mismatched, stop and report the exact problem. Do not create, remove, switch, reset, merge, or repair worktrees or branches.
6. Before changing files, run `./mvnw clean verify` in each assigned worktree. Record any pre-existing failures as baseline failures and do not mask them with unrelated changes.

## Reserve shared verification resources

1. Before assigning writers, inspect the relevant build, test, and runtime configuration for fixed shared resources: ports, databases, filesystem paths, Docker resources, and external services.
2. Record the owner and isolation strategy for every shared resource. If the resource cannot be isolated safely, reserve a serialized verification window before implementation begins. Do not change application configuration merely to accommodate parallel orchestration.
3. Give writers any resource reservation and the command sequence they must follow. A candidate must not run a reserved command outside its assigned window.

For any required verification that fails only because the current sandbox lacks necessary access, request one scoped approval to rerun the exact command with the necessary access when the session supports approvals. Do not request broader access, a broader sandbox mode, or unrelated filesystem or network access. If approval is unavailable or declined, record the failure as an environmental blocker and continue only where that blocker does not invalidate the result.

## Coordinate implementation

1. Assign exactly one writing implementation agent to each worktree. Never allow concurrent writers in the same worktree.
2. Give the primary agent the issue URL and canonical matrix, and instruct it to use `$implement-issue` for the simplest design consistent with existing conventions.
3. Give the alternative agent the issue URL and canonical matrix, and instruct it to use `$implement-issue` for a meaningfully different viable design, or to explain why no useful alternative exists without changing files.
4. Require each writer to return after its test-plan gate, before production changes, with every matrix ID mapped to a proposed test class and method. Audit that plan against the canonical matrix. If it is incomplete or cannot prove a row, return it for correction before implementation; this is planning, not a repair cycle.
5. Treat a writing agent’s implementation handoff as a checkpoint, not completion. Require it to complete the shared matrix with exact test class and method, starting state and action, final result asserted, and one verdict per in-scope row: `PASS`, `FAIL`, or `BLOCKED`. For user-facing flows, evidence must follow redirects and inspect the final rendered content. For migrations, evidence must exercise the actual database constraint. For snapshots, evidence must assert every required snapshot field.
6. Audit every matrix ID against the issue and the test code before commissioning independent review. Publish one exhaustive verdict: `PASS`, `FAIL`, `BLOCKED`, or `NOT APPLICABLE` only for an explicit issue exclusion. A passing build, implementation summary, or claim that a scenario is covered is not evidence by itself.
7. If the audit reports any `FAIL` or `BLOCKED` row, send the same writer one focused repair task containing the complete current finding set. Require it to implement the repair, add the missing evidence, rerun relevant verification, and return an updated matrix. Do not hand off a candidate to independent review while any in-scope row is not `PASS`.
8. Each candidate has two total repair cycles after its implementation checkpoint. A repair cycle addresses a complete audit finding set, not one finding at a time. After a repair, repeat the exhaustive evidence audit; if it passes, commission independent review. A newly discovered gap consumes a cycle only when it was introduced by the repair or was not reasonably detectable in the prior audit. If material gaps remain after the second repair cycle, the writer identifies a genuine blocker, or final verification fails, mark the candidate ineligible and report the unresolved matrix IDs. Do not waive a finding or start a third repair cycle.
9. Allow additional agents only for read-only investigation or review after the writing agent has finished a checkpoint. If subagents are unavailable, coordinate the two worktrees sequentially; do not launch nested Codex processes.

## Verify and hand off

1. Require each changed worktree to add or update tests, run `./mvnw clean verify` after implementation, inspect its final diff, and provide the completed shared matrix. Do not accept a passing build or an intermediate-response test as evidence that a user journey is complete.
2. Commission an independent, read-only `$review-implementation` review for every candidate whose evidence audit marks every in-scope matrix row `PASS`; this is an audit candidate, not yet a candidate eligible for selection. Give the reviewer only its assigned worktree, exact common base commit, issue, canonical matrix, explicit exclusions, and relevant verification commands. Do not give it the writer's self-assessment, completed matrix, another candidate's implementation, or another reviewer's findings.
3. Require a complete review report with exactly one terminal verdict: `ACCEPT`, `CHANGES_REQUESTED`, or `BLOCKED`. Reviewers inspect the actual diff and test code, not an implementation summary. A reviewer does not select a winner.
4. Audit reviewer findings against the issue and explicit exclusions. Reject only findings that concern an explicit exclusion or invent a requirement absent from the issue. Do not reinterpret a valid finding as follow-up work to make a candidate selectable.
5. For `CHANGES_REQUESTED`, send the original writer one consolidated repair task containing the complete valid finding set. The writer maps every finding to code or test changes and verification in its response. Allow one review-driven repair cycle only, then send the updated candidate to the same independent reviewer for re-review. If material findings remain, mark the candidate ineligible; do not start another review-driven repair cycle.
6. Mark a candidate eligible for selection only when its evidence audit and final independent review have no unresolved material findings. If neither candidate is eligible, report that outcome and the specific blockers; do not select the least-bad implementation.
7. Emit a compact evidence index for each candidate mapping every matrix ID to its exact test method(s), relevant file(s), and final verdict. A passing build or a claimed matrix row is not an evidence index.
8. Leave eligible changed worktrees ready to commit after final verification. Collect a concise handoff from each candidate: implementation summary, tests and outcomes, evidence index, explicit exclusions, assumptions, risks, unresolved blockers, and whether it produced a commit.
9. Do not merge, delete, or clean up branches or worktrees. Hand eligible candidates to the selection workflow.
