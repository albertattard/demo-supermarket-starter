---
name: implement-issue
description: Implement a GitHub issue or ticket in the current repository. Use when the user asks to implement a ready issue, story, or bug; inspect relevant code and tests; make a focused change; verify it; and report the result.
---

# Implement Issue

## Establish the task

1. Read the issue title, body, comments, linked work, and applicable repository instructions.
2. Inspect only the code, tests, and documentation necessary to understand the affected behaviour and existing conventions.
3. Treat the issue requirements as the scope boundary. Extract explicit in-scope requirements, explicit exclusions and follow-up work, and repository assumptions into separate lists. Do not add unrelated refactors, speculative features, or work explicitly assigned to a follow-up issue.
4. Before changing files, inspect the worktree status. Treat pre-existing changes as user-owned unless clearly part of the assigned issue. Do not modify, reformat, stage, or rely on them without stating how they affect the implementation and verification.
5. Do not treat existing tests as proof that the issue is complete. Compare them directly with the issue’s acceptance scenarios and identify untested observable behaviour.

If a material ambiguity remains that affects user-visible behaviour, security, data, compatibility, or scope, ask exactly one concise clarification question and stop. Otherwise, make the smallest defensible engineering choice and state it in the final summary.

## Establish a baseline

1. Run `./mvnw clean verify` before making changes.
2. If verification fails, determine whether the failure is related to the issue. Do not attribute a pre-existing failure to the implementation; report it and avoid masking it with unrelated changes.

For any required verification that fails only because the current sandbox lacks necessary access, request one scoped approval to rerun the exact command with the necessary access when the session supports approvals. Do not request broader access, a broader sandbox mode, or unrelated filesystem or network access. If approval is unavailable or declined, record the failure as an environmental blocker and continue only where that blocker does not invalidate the result.

## Define and validate the evidence contract

Before changing production code, create a requirements-to-evidence matrix. Assign stable IDs to every acceptance criterion and every specific observable, data, migration, security, or integrity requirement from the issue scope that the acceptance criteria do not repeat. Record explicit exclusions separately with their stated follow-up, if any. An excluded requirement must never be reported as an implementation gap.

For each requirement, record:

- its kind: user journey, HTTP/access, domain, persistence, migration, or integration;
- the exact test class and method that will prove it;
- the starting state and action; and
- the required final result: status and redirect target where applicable, rendered content for user-facing flows, and persisted state or rejected write where applicable.

For each excluded requirement, record its reason and follow-up issue or work item. For each assumption, record why it is needed and how it affects implementation or verification.

Do not mark a requirement covered by an adjacent lower-level test. For a redirecting flow, follow the redirect and assert the final rendered result. For a database constraint, exercise the actual migrated database with a write that must fail. For a snapshot, assert each field the issue requires, rather than only an aggregate or a representative field.

Before changing production code, inspect the matrix critically. Every in-scope row needs a credible proof plan; if it does not, refine the test strategy or ask the required clarification question. Do not defer mandatory rows to the final verification pass.

## Design and implement

1. For user-facing changes, write each acceptance scenario as a complete interaction: initial request or screen → user action → redirects or navigation → final observable result. Identify each boundary that can alter the outcome, including authentication, authorization, sessions, CSRF, validation, and external calls.
2. For changes that add or alter HTTP routes, record the intended access level for each affected route: public, authenticated, role-restricted, or internal. Add a test for each newly public or newly protected route. Never broaden a wildcard matcher merely to make one route work.
3. Work backwards from the acceptance scenario through the controller or boundary, application logic, and persistence or integrations. Do not force a frontend-first sequence for backend-only, migration, operational, or infrastructure work.
4. Before changing production code, write or update the test skeleton for every in-scope matrix row, then run the focused tests and record their expected failures. A shared test may cover multiple rows only when it explicitly asserts every required final result. For user-facing acceptance scenarios, use an end-to-end user-journey test: begin at the relevant user entry point, perform the user action, follow navigation or redirects, and assert the final visible outcome. Controller, MockMvc, service, or other lower-level tests may supplement that journey test, but must not replace it. For backend-only, migration, operational, or infrastructure work, use the lowest-level test that exercises all relevant boundaries.
5. Make the smallest change that makes the test pass.
6. Refactor only after the relevant tests pass, while keeping the change within the issue's scope.

## Verify

1. Run the most relevant verification available in the repository. Investigate and resolve failures caused by the change; clearly report unrelated pre-existing failures.
2. For every changed public route or user journey, verify its intended access level anonymously and, where relevant, as an authenticated user. Do not infer route accessibility from controller tests alone.
3. Complete the evidence matrix with the tests actually run. Re-read the issue and final diff, then give every in-scope row exactly one verdict: `PASS`, `FAIL`, or `BLOCKED`. Reject any row whose test does not prove the stated final result. Do not use `NOT APPLICABLE` for an in-scope row; reserve it for explicitly excluded work.
4. Review the final diff for scope creep, regressions, accidental generated files, and missing tests.

## Finish

Summarize:

- what changed;
- tests or verification run and their outcome;
- the completed requirements-to-evidence matrix, including the verdict and evidence for every in-scope row, explicit exclusions, and explicit uncovered requirements when any remain;
- explicit engineering assumptions;
- remaining risks, follow-ups, or blockers.

Do not claim completion when a required matrix row is uncovered, required verification has not been run, or a material ambiguity remains unresolved.
