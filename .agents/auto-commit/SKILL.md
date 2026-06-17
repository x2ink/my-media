---
name: auto-commit
description: Automatically generates conventional commit messages based on staged Git changes and commits them locally, respecting repository Git hooks.
---

# Auto-Commit Skill

This skill allows the agent to analyze staged changes in the Git repository, draft an intelligent and clear commit message adhering to the **Conventional Commits** standard, and commit the changes locally while complying with git hooks.

---

## 1. Check Staged Changes

First, retrieve the exact diff of the files currently staged for commit:
```bash
git diff --cached
```

*   If the output is empty, it means no changes are currently staged. In this case, notify the user and ask them to stage files (`git add <file>`) before running the commit tool.

---

## 2. Generate the Commit Message

Analyze the staged changes and draft a commit message following the **Conventional Commits v1.0.0** specification.

### Format
```text
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

### Staging-Area Alignment Constraint
*   **No Chat/Context Bias**: Do NOT base the commit message on conversational history, debugging discussions, or background chat context (e.g., do not mention fixing specific crashes or bugs like "ANR", "NPE", "NullPointerException", or "memory leaks" unless they are explicitly and directly visible in the code changes being committed).
*   **Staging Area Only**: The commit message must describe *only* the physical code modifications present in the staged diff (`git diff --cached`). Do not speculate on intention beyond what is code-evident.

### Type Guidelines
Choose the appropriate type:
*   `feat`: A new feature or capability.
*   `fix`: A bug fix.
*   `docs`: Changes to documentation (e.g., Markdown files, Javadocs).
*   `style`: Formatting, missing semi-colons, code styling (no functional changes).
*   `refactor`: Code changes that neither fix a bug nor add a feature.
*   `perf`: Code changes that improve performance.
*   `test`: Adding or updating test suites.
*   `build`: Changes affecting the build system, Gradle files, or dependencies.
*   `chore`: Updating build tasks, local configurations, or package files (no src changes).
*   `revert`: Reverting a previous commit.

### Style & Length
*   **Subject line**: Clear and concise description, maximum 50-72 characters. Use the imperative, present tense (e.g., "add library support" instead of "added library support"). Do not capitalize the first letter, and do not end with a period.
*   **Body (Optional)**: If the changes are complex, add a detailed description explaining the "why" and "what", separating it from the subject line by a blank line.

---

## 3. Execute Commit & Handle Git Hooks

Commit the changes locally using the generated message:
```bash
git commit -m "<commit_message>"
```

### Complying with Git Hooks
1.  **Do not bypass hooks**: Do NOT use `--no-verify` or `-n`. The commit must run through the repository's git hooks (e.g., pre-commit, commit-msg) to ensure code quality and style compliance.
2.  **Handle failures gracefully**:
    *   If the commit fails due to a **pre-commit** hook (e.g., lint errors, static analysis):
        *   Read the error log.
        *   Fix the offending code lines/formatting.
        *   Re-stage the modified files (`git add`).
        *   Attempt the commit again.
    *   If the commit fails due to a **commit-msg** hook (e.g., strict message formatting checks):
        *   Read the failure output to understand the format rules.
        *   Revise the commit message to conform to the hook's requirements.
        *   Attempt the commit again.

---

## 4. Verification

After a successful commit, verify the repository status:
```bash
git status
```
Confirm that:
1.  Staged changes have been successfully committed.
2.  No changes are left in the staging area.
3.  Do **NOT** push to remote. Leave the pushing step to the user.
