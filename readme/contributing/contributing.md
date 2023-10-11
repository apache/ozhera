# PR Submission Guidelines

## 1.Create Issues

###   （1）Navigate to the main Issues page

![1-1.PNG](images%2F1-1.PNG)

### （2）Click the green 'New issue' button to start creating

![1-2.PNG](images%2F1-2.PNG)
access the page as shown below
![1-2-1.PNG](images%2F1-2-1.PNG)

### （3）Assign to a Reviewers or Assignees(Optional)

![1-3.PNG](images%2F1-3.PNG)

### （4）Set Labels (Indicate the Type of Requirement)

There are a total of 9 major labels, and you can choose multiple, but in most cases, select one. If it's a bug, choose 'bug'.
![1-4.PNG](images%2F1-4.PNG)

### （5）After selecting the labels, proceed to write the Title

The title has a specific format requirement and must follow the format [Label] Description. For example, if the label is 'bug,' then the title should be: [bug] Issue with clicking on 'Get Verification Code.' Please note: there should be a space after [ ].
![1-5.PNG](images%2F1-5.PNG)

### （6）Write Content

Expand the title to clarify the issue, which can be in text or image format (you can paste images directly). When multiple requirements are mixed together and multiple checkboxes are needed, it should be written as '(Optional)'

```Plaintext
- [ ] (Input Box） boolean
```

![1-6.PNG](images%2F1-6.PNG)

## 2.Create pr

###   (1) Forking a project from an open-source repository to your own remote repository.

![2-1.PNG](images%2F2-1.PNG)

### (2) Clone the project from your own repository to your local machine.

![2-2.PNG](images%2F2-2.PNG)

### (3) Remember to regularly sync remote code to your local repository.

![2-3.PNG](images%2F2-3.PNG)


### (4) Create a new branch locally from the master branch, make code changes, submit them to the remote repository, and then click on 'pull request' to create a PR

![2-4.PNG](images%2F2-4.PNG)

### (5) Create a proper PR (Pull Request).

![2-5.PNG](images%2F2-5.PNG)
In the PR request, you can include '#702' or 'close #702'. This way, when your PR is merged, the issue will also be automatically marked as closed. #702 represents the issue number, which can be found in the URL after creating the issue, such as...
![2-5-1.PNG](images%2F2-5-1.PNG)

### (6) Specify Reviewers and Assignees, it must be specified.

![2-6.PNG](images%2F2-6.PNG)

```Apache
On GitHub, "reviewer" and "assignee" are two different roles that have different roles in Pull Request (PR) and Issue management:  Reviewer:  A reviewer is a person who is asked to review and evaluate a Pull Request. A reviewer is usually responsible for viewing code changes to a PR, making recommendations, checking the quality and maintainability of the code, and ultimately approving or rejecting the merger of a PR.  PR authors or other collaborators can choose to assign one or more reviewers to the PR, so that they review.  The role of the reviewer is to ensure the quality, consistency and maintainability of the code, and to help the team ensure that the code meets the standards and requirements of the project.  Assignee (Designator):  A designee is a person designated as a responsible person for an Issue or PR, who is responsible for tracking and resolving issues or merging PR.  Typically, the appointee can be the creator of the Issue or PR, a member of the project team, or someone else as needed.  The role of the designee is to ensure that the problem or PR is given appropriate attention and treatment. They may need to coordinate and follow up work to ensure that the problem is resolved or the PR is merged within the appropriate time.  In summary, reviewers are mainly related to code review and merging, while assignors are mainly related to the allocation and tracking of tasks and problems. In actual collaboration, these two roles usually work together to ensure that the code and problems of the project are properly handled and resolved.

```

### (7) Find the corresponding individuals above to Approve your changes, and then ask the Reviewer to merge your changes

![2-7.PNG](images%2F2-7.PNG)
![2-7-1.PNG](images%2F2-7-1.PNG)

## 3.close Issues

### (1) Automatically close the Issue.

When your PR is merged, if the PR's content includes 'close #issueId', the corresponding issue will also be marked as closed
![3-1-1.PNG](images%2F3-1-1.PNG)
![3-1-2.PNG](images%2F3-1-2.PNG)

### (2) Manually merge the PR and close the Issue.

![3-2-1.PNG](images%2F3-2-1.PNG)

```SQL
"Squash and merge" is a useful merge option, especially for projects that want to maintain a clean commit history. It allows you to combine multiple small commits into a more organized single commit, reducing noise and clutter in the branch history. However, please note that the merged commit history will no longer include every individual commit from the original pull request branch, so some detailed information may be lost.
```

When merging PRs, it's important to select the "Squash and merge" option. This way, the PR ID will be appended to the commit message on the homepage, allowing you to directly navigate to the PR page using the message. This helps to effectively link what issue the commit addresses and how it was resolved.

![3-2-2.PNG](images%2F3-2-2.PNG)
![3-2-3.PNG](images%2F3-2-3.PNG)