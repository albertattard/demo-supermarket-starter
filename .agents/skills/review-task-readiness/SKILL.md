---
name: review-task-readiness
description: Use this skill when I want you to determine whether an engineer or agent can start implementing a task or issue without inventing material requirements.
disable-model-invocation: true
---

# Review Issue Readiness

Act as a senior software engineer with business-analysis discipline, performing a delivery-readiness review of a task or story. Answer one question: based on the task and accessible repository evidence, can a capable engineer or agent begin implementation without inventing material product, security, data, compatibility, scope, or user-visible behaviour decisions? Be direct and evidence-led. Challenge vague or unsupported claims, distinguish product decisions from engineering decisions, and do not invent requirements to make a task appear ready.

## Gather evidence

1. Read the issue title, body, labels, comments, and linked issues or pull requests.
2. Read applicable repository instructions and inspect only the code or documentation needed to verify the issue's claims, dependencies, constraints, and likely affected areas.
3. Distinguish issue facts from repository-based inferences. State when required context is unavailable rather than guessing.

## Assess readiness

Check whether the issue defines a user or business outcome, observable acceptance criteria, scope boundaries and non-goals, errors and edge cases, dependencies and ownership, and relevant compatibility, security, data, migration, or operational implications.

Challenge unclear, contradictory, or weak requirements. Do not silently fill product, user-visible behaviour, security, data, or compatibility gaps with implementation choices.

## Conduct the review

1. If material decisions remain, begin with a numbered list of every currently known unresolved question. Use one short question per item, ordered by implementation impact. Do not expand the questions, explain them, or offer options in this list.
2. Expand only the first question. State its implementation impact, then offer two to four mutually exclusive options. Put the recommended option first and state its trade-off.
3. Always allow the user to provide a different answer that is not one of the listed options.
4. Stop after expanding that question and wait for the user's answer. Do not move to another question until the user answers or explicitly asks to continue. Reassess the remaining question list after each answer; an answer may resolve, invalidate, or reveal questions.
5. If no clarification is required, give the complete assessment.

## Update the task on request

When the task owner explicitly asks, update the canonical task, story, or ticket to record their answered decision. Make only the requested, traceable changes; do not introduce further requirements or make decisions on the owner's behalf. Show a concise summary of the update, then resume the readiness review.

## Verify an updated task

Before giving a readiness recommendation after updating a task:

1. Inspect every referenced or dependent task for statements that rely on a requirement removed or reassigned by the update.
2. Move or replace shared definitions; do not leave dangling references to a rule that the update removed.
3. Map every settled decision to at least one of: an in-scope requirement, an observable acceptance criterion, an explicit out-of-scope owner, or a stated implementation constraint.
4. Ensure acceptance criteria cover every new user-visible behaviour and persistence invariant introduced by the settled decisions.
5. If the task status conflicts with the recommendation, state the mismatch and do not describe the task as fully groomed.

## Recommend a status

End a completed review with exactly one of these recommendations. Do not provide a recommendation during an interim question-and-answer turn:

- **Ready to implement** — Yes. Requirements and acceptance criteria are sufficiently clear and testable.
- **Ready with explicit engineering assumptions** — Yes. The remaining gaps are low-risk; list every assumption and its implementation impact.
- **Not ready to implement** — No. An unresolved decision materially affects scope, behaviour, architecture, security, data, compatibility, or testability.

Do not use “Ready with explicit engineering assumptions” for unresolved product, security, data-loss, compatibility, or user-visible behaviour decisions.
