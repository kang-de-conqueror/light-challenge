# Approval workflow challenge
At Light, we want to implement the best in class invoice approval workflow application.
Every time one of our customers receives an invoice from a vendor, an approval request is sent to one more employees (approvers).

The decision making about whom to send the approval request can only be based of the following:
- the invoice amount.
- department the invoice is sent to.
- whether the invoice requires manager approval.

It could be all of these items, or any subset of them.

**For this challenge we want you to implement the logic to support the worflow described on the figure bellow:**

![code_exercise_diagram (2)](https://user-images.githubusercontent.com/112865589/191920630-6c4e8f8e-a8d9-42c2-b31e-ab2c881ed297.jpg)

Fig. 1

Please note that while you could implement it by simply hardcoding all those conditions, that is not what we are looking for. You don't need to provide a way to update the workflow for this challenge, but we are looking for a design that is dynamic and would allow for updating any of the steps in the workflow.

## Challenge requirements

This is the list of things you should provide:

- A database model to support the workflow configuration and execution (a jpeg of the database schema can be put in the README file)
  - Don't worry about implementing this part, everything can be done in memory for the challenge, we only want to see how you would design the database to support this.

- The logic to process the workflow described in the figure above and an http endpoint to call and execute the workflow:
  - The endpoint should allow passing invoice amount, department and if a manager approval is required as input fields.
  - Don't worry about the notification logic, simply printing `"sending approval via Slack"` is enough.

- A simple UI to call the endpoint defined above and execute the workflow.

Don't worry about currency support, you can assume everything is in USD.

Any other consideration and things you would do better/different in a real world scenario feel free to put in the README, no need to implement it.

## Provided code

We provide a basic backend setup in Kotlin, as well as placeholders in the code for you to fill. This is just a suggestion, and **you're welcome to use a different structure and/or language.**.

If you do decide to use another language, **any OO language is fine**.

### How to build & run

## Backend

```sh
cd backend
./gradlew clean build
./gradlew run
```

## Frontend

```sh
cd frontend
npm install
npm run dev
```

The frontend server provides a proxy to the backend on the `/api` path so you don't run into CORS issues. It expects the backend to be running on port 8080. If you need to change that, update the config on `next.config.js`.

### Submitting your implementation

1. Commit all your changes.
2. Run `git bundle create challenge-<your-name>.bundle --all`
3. Send us the generated bundle file
