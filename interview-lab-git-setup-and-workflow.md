# Git & GitHub Setup — Interview Lab

This document records the complete Git/GitHub setup and workflow used for the **Interview Lab** Spring Boot project, starting from creating the project folder/repository up to the current Git workflow.

---

## 1. Create the Project Folder

We created the project folder:

```text
interview-lab
```

Example location on Windows:

```text
Desktop/
└── uday/
    └── hands on projects/
        └── interview-lab/
```

Open Git Bash inside the project folder:

```bash
cd ~/Desktop/uday/"hands on projects"/interview-lab
```

---

# 2. Initialize Git

Inside the project folder:

```bash
git init
```

This creates the local Git repository.

Conceptually:

```text
Project Folder
      ↓
   git init
      ↓
Local Git Repository
```

A hidden `.git` directory is created.

---

# 3. Check Git Status

```bash
git status
```

Initially, project files appear as **untracked files**.

Example:

```text
Untracked files:
    .gitattributes
    .gitignore
    .mvn/
    mvnw
    mvnw.cmd
    pom.xml
    src/
```

---

# 4. Configure Git Identity

Before creating a commit, Git needs the author's name and email.

Set them globally:

```bash
git config --global user.name "Your Name"
git config --global user.email "YOUR_GITHUB_EMAIL"
```

Verify:

```bash
git config --global user.name
git config --global user.email
```

### Why?

Every Git commit stores author information.

```text
Commit
 ├── Author name
 ├── Author email
 ├── Commit message
 └── Snapshot of project changes
```

---

# 5. Stage Project Files

To stage all project files:

```bash
git add .
```

Conceptually:

```text
Working Directory
       ↓
   git add .
       ↓
Staging Area
```

Check:

```bash
git status
```

---

# 6. Create the Initial Commit

Create a commit:

```bash
git commit -m "Initial Spring Boot project setup"
```

Later, our project used meaningful milestone commit messages such as:

```bash
git commit -m "step 6: transaction basic and rollbackFor and noRollbackFor completed"
```

A commit is a saved snapshot in the local Git repository.

```text
Staging Area
      ↓
   git commit
      ↓
Local Repository
```

---

# 7. Rename / Set the Main Branch

Use `main` as the primary branch:

```bash
git branch -M main
```

Check:

```bash
git branch
```

Expected:

```text
* main
```

---

# 8. Create the GitHub Repository

Create a repository on GitHub:

```text
interview-lab
```

For an existing local project, create the GitHub repository without adding another initial README, `.gitignore`, or license.

The local project already contains those files.

---

# 9. Connect Local Repository to GitHub

Add the GitHub repository as the remote named `origin`:

```bash
git remote add origin https://github.com/<username>/interview-lab.git
```

Check:

```bash
git remote -v
```

Conceptually:

```text
Local Repository
       │
       │ origin
       ▼
GitHub Repository
```

---

# 10. First Push to GitHub

Push the local `main` branch:

```bash
git push -u origin main
```

### Meaning of `-u`

It establishes the upstream relationship:

```text
local main
    ↓
origin/main
```

After this, future pushes can usually be done with:

```bash
git push
```

---

# 11. First Push Error — `src refspec main does not match any`

We encountered:

```text
error: src refspec main does not match any
```

The reason was:

```text
main branch existed
BUT
there was no commit yet
```

`git push` needs a commit/reference to push.

Correct sequence:

```bash
git add .
git commit -m "Initial Spring Boot project setup"
git push -u origin main
```

Important distinction:

```text
git add
    ↓
Stage changes

git commit
    ↓
Save snapshot locally

git push
    ↓
Send commits to GitHub
```

---

# 12. Git Identity Error

We also encountered:

```text
Author identity unknown

Please tell me who you are.
```

Solution:

```bash
git config --global user.name "Your Name"
git config --global user.email "YOUR_GITHUB_EMAIL"
```

Then retry:

```bash
git commit -m "..."
```

---

# 13. LF / CRLF Warning on Windows

We saw:

```text
warning: in the working copy of 'README.md',
LF will be replaced by CRLF the next time Git touches it
```

This is a line-ending warning related to Windows.

It is **not a commit failure**.

The commit can still succeed.

---

# 14. Check Commit History

To see recent commits:

```bash
git log --oneline
```

Example:

```text
1dbf9e4 Update README with transaction experiments
b3ac6c4 step 6: transaction basic and rollbackFor and noRollbackFor completed
1888bc4 step 6: fetch join and EntityGraph
73e359d Step 6.5: Fetch Eager and Lazy completed
```

For a graph view:

```bash
git log --oneline --decorate --graph -5
```

---

# 15. Compare Two Commits

To see the actual code difference between two commits:

```bash
git diff <commit1> <commit2>
```

Example:

```bash
git diff b3ac6c4 1dbf9e4
```

### Only changed file names

```bash
git diff --name-only <commit1> <commit2>
```

### Files + statistics

```bash
git diff --stat <commit1> <commit2>
```

### Specific file

```bash
git diff <commit1> <commit2> -- README.md
```

---

# 16. `git show` — Inspect One Commit

To see what a particular commit changed:

```bash
git show <commit>
```

Example:

```bash
git show 1dbf9e4
```

Difference:

```text
git log
    → commit history

git show <commit>
    → changes made by one commit

git diff A B
    → differences between two commits
```

---

# 17. If a File Was Missed From the Last Commit

Suppose the last commit was already created but one file was accidentally left out.

Stage the missing file:

```bash
git add path/to/missing-file
```

Then amend the previous commit:

```bash
git commit --amend --no-edit
```

`--no-edit` keeps the previous commit message.

Conceptually:

```text
Old commit
    ↓
git add missing-file
    ↓
git commit --amend
    ↓
Updated last commit
```

---

# 18. Important: Amend Changes the Commit Hash

When we use:

```bash
git commit --amend
```

Git creates a new version of the commit.

Therefore:

```text
Old commit
    ↓
Old hash

Amend
    ↓
New commit
    ↓
New hash
```

This becomes important if the old commit has already been pushed to GitHub.

---

# 19. If an Amended Commit Was Already Pushed

If the amended commit replaces a commit that already exists on the remote, a normal:

```bash
git push
```

may fail with:

```text
non-fast-forward
```

In that situation, **do not immediately force push**.

First verify the local and remote histories.

Useful commands:

```bash
git fetch origin
```

```bash
git log --oneline --decorate --graph --all -10
```

```bash
git diff <remote-branch> <local-branch>
```

---

# 20. `--force-with-lease`

If the amended local history is intentionally supposed to replace the remote branch, prefer:

```bash
git push --force-with-lease origin main
```

instead of:

```bash
git push --force
```

### Why?

`--force-with-lease` provides a safety check.

It essentially says:

> Replace the remote branch only if the remote branch is still in the state I expect.

This is safer than blindly using:

```bash
git push --force
```

---

# 21. Our Safer Alternative — Create a New Commit

In our project, we wanted to avoid rewriting the remote history.

We first fetched the remote:

```bash
git fetch origin
```

Then created a backup reference:

```bash
git branch remote-backup origin/main
```

We compared the remote and local states:

```bash
git diff --name-status remote-backup main
```

The result was:

```text
M README.md
```

So only `README.md` differed.

Then we moved the local branch back to the remote state while preserving the changes:

```bash
git reset --soft remote-backup
```

Important:

```text
--soft
```

preserves the changes in the staging area.

Then:

```bash
git status
```

showed:

```text
Changes to be committed:
    modified: README.md
```

We created a normal new commit:

```bash
git commit -m "Update README with transaction experiments"
```

This created:

```text
1dbf9e4 Update README with transaction experiments
```

Then:

```bash
git push origin main
```

Initially GitHub returned:

```text
remote: fatal error in commit_refs
! [remote rejected] main -> main (failure)
```

We verified that the local and remote histories were correct:

```bash
git status
git log --oneline --decorate --graph -5
git ls-remote origin refs/heads/main
```

After retrying:

```bash
git push origin main
```

the push succeeded.

---

# 22. Why `git reset --soft` Was Safe Here

We used:

```bash
git reset --soft remote-backup
```

not:

```bash
git reset --hard
```

### `--soft`

Moves HEAD but keeps changes staged.

```text
Commit history
      ↓
HEAD moves

Files
      ↓
Changes preserved
```

### `--hard`

Moves HEAD and resets working files.

```text
Commit history
      ↓
HEAD moves

Files
      ↓
Can be overwritten
```

For a "do not lose my changes" situation, `--soft` is much safer when the intention is to turn the changes into a new commit.

---

# 23. Backup References Used During the Git Recovery

We created:

```bash
git branch remote-backup origin/main
```

This preserved the GitHub state as a local reference.

We also used:

```bash
git tag before-force-push
```

to preserve a reference to the local commit before any potential force push.

These references helped us compare and recover states safely.

---

# 24. Current Git Workflow — IMPORTANT

## Normal Change

For normal project changes:

```bash
git add .
git commit -m "..."
git push
```

Flow:

```text
Working Directory
      ↓
git add .
      ↓
Staging Area
      ↓
git commit
      ↓
Local Repository
      ↓
git push
      ↓
GitHub
```

---

## Last Commit Mein File Miss Ho Gayi

If the last commit has not created a problem with shared remote history:

```bash
git add <file>
git commit --amend --no-edit
```

---

## Amend Already Pushed to Remote

Do **not** blindly force push.

First verify:

```bash
git fetch origin
git log --oneline --decorate --graph --all
git diff origin/main main
```

If you intentionally want to replace the remote history:

```bash
git push --force-with-lease origin main
```

Prefer:

```text
--force-with-lease
```

over:

```text
--force
```

---

## Want to Avoid Force Push

If you want to preserve the existing remote history and create a new commit instead:

```bash
git fetch origin
git branch remote-backup origin/main

git reset --soft remote-backup

git status

git commit -m "..."

git push origin main
```

Conceptually:

```text
Remote:
A ──→ B

Local amended state:
A ──→ B'

Instead of replacing B:

A ──→ B ──→ C
             ↑
          new commit
```

This preserves the existing remote commit history.

---

# 25. Quick Git Cheat Sheet

| Requirement | Command |
|---|---|
| Initialize repository | `git init` |
| Check status | `git status` |
| Stage all | `git add .` |
| Stage one file | `git add <file>` |
| Commit | `git commit -m "message"` |
| Amend last commit | `git commit --amend --no-edit` |
| Show history | `git log --oneline` |
| Show graph | `git log --oneline --decorate --graph` |
| Show one commit | `git show <commit>` |
| Compare commits | `git diff A B` |
| Fetch remote | `git fetch origin` |
| Add remote | `git remote add origin <URL>` |
| Normal push | `git push` |
| First push | `git push -u origin main` |
| Safer force push | `git push --force-with-lease origin main` |
| Preserve changes while moving HEAD | `git reset --soft <commit>` |
| Create backup branch | `git branch <name> <commit>` |
| Create backup tag | `git tag <name>` |

---

# 26. Golden Rules

### Rule 1

```bash
git add
```

does **not** create a commit.

### Rule 2

```bash
git commit
```

does **not** push to GitHub.

### Rule 3

```bash
git push
```

sends commits to the remote repository.

### Rule 4

Before using force push:

```text
STOP
 ↓
fetch
 ↓
inspect
 ↓
verify
 ↓
then decide
```

### Rule 5

For a shared repository, prefer:

```bash
git push --force-with-lease
```

over:

```bash
git push --force
```

### Rule 6

If you want to avoid rewriting remote history:

```text
remote history
      ↓
preserve it
      ↓
create a new commit
      ↓
normal push
```

---

# Current Project Git State

Our Interview Lab repository currently has this history pattern:

```text
main
 │
 ├── Latest project milestone commits
 │
 ├── JPA / EntityGraph / Fetch experiments
 │
 ├── Transaction / rollback experiments
 │
 └── README updates
```

The Git/GitHub workflow is now part of the project's practical interview preparation as well.
