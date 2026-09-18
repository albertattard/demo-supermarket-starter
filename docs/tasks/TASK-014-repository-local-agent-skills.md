---
id: TASK-014
title: Add repository-local agent skills
status: done
depends_on: [TASK-001]
---
## Goal

Add the repository-local agent skills that support the workshop workflow while keeping their purpose and trust boundaries visible in version control.

The skills are operational guidance, not automatically trusted automation. A contributor must inspect a skill and decide whether to invoke it for a specific task.

## Scope

- Add these skill packages under `.agents/skills/`:
  - `commit-changes`
  - `implement-issue`
  - `orchestrate-issue-implementations`
  - `review-implementation`
  - `review-task-readiness`
- Include a `SKILL.md` and `agents/openai.yaml` metadata file in every package.
- Keep the skill package in a dedicated commit after the project foundation so `git log -- <skill-path>` identifies its introduction clearly.
- Provide the `review-task-readiness` skill for gathering task and repository evidence, separating unresolved decisions, and waiting for task-owner input before an authorised task update.
- Require contributors to review the relevant package before invoking it and to treat Git history, static checks, and model output as evidence rather than approval.

## Out of Scope

- Automatically invoking a skill during repository setup
- Treating a tracked or clean skill package as trusted without human review
- Downloading, installing, or executing instructions from external skill sources
- Creating GitHub workflows, pull requests, or other remote automation
- Updating a task without explicit task-owner authorisation

## Acceptance Criteria

- Each named package contains both `.agents/skills/<name>/SKILL.md` and `.agents/skills/<name>/agents/openai.yaml`.
- The skills are introduced in a dedicated commit after `TASK-001`’s project foundation commit.
- `git diff --exit-code HEAD -- .agents/skills` succeeds before a contributor relies on the packaged skill revision.
- `git ls-files --error-unmatch` confirms the expected files for a reviewed skill package are tracked.
- A contributor can identify the latest commit that changed a reviewed skill package with `git log -1 -- <skill-path>`.
- Skill checks and model assessments are treated as review evidence; the task owner makes the approval or rejection decision.
