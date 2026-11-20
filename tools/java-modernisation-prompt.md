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
- Do not modify pom.xml files, .mvn, mvnw, or mvnw.cmd.
- Do not run Maven verify, including ./mvnw clean verify. The controller runs the authoritative verification independently.
- Respect the existing execution and network policy. Do not request approval or attempt to bypass restrictions in this unattended run.

Process:
1. Inspect the codebase and relevant tests. Select the strongest single candidate, if one exists.
2. If no candidate provides a material benefit, do not change source files. Return the no-improvement hand-off below.
3. Otherwise, implement only the selected change. Add or adjust tests where necessary to demonstrate that behaviour remains correct.
4. Run ./mvnw test. Resolve failures caused by the change. If execution or dependency downloads are blocked, record validation as blocked; if tests cannot be attempted, record not-run. Do not claim tests passed unless they did.
5. Review the final unstaged diff for scope and correctness.
6. If a worthwhile change cannot be safely implemented within these constraints, stop and return the blocked hand-off with a concrete action for an engineer. Report any partial changes in the summary. Blocked or unattempted worker tests alone do not require a blocked outcome: the controller verifies implemented changes independently.

Final response:
Output only valid JSON: no Markdown fences, explanation, or surrounding text.

If no improvement is justified:

{"outcome":"no-improvement","summary":"<why no candidate justified a change>"}

If a worthwhile change requires engineer action before implementation can be completed:

{"outcome":"blocked","summary":"<what prevented completion, including any partial changes>","required_action":"<concrete action for an engineer before rerunning>"}

If you implemented a modernisation (the controller will independently verify it):

{"outcome":"modernised","title":"<concise pull-request title>","commit_message":"<imperative commit subject>","summary":"<what changed and why it is worthwhile>","files_changed":["<repository-relative path>"],"validation":[{"command":"./mvnw test","result":"<passed|blocked|not-run>"}],"review_focus":"<compatibility risks or review considerations>"}
