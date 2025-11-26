# Invoice Approval Workflow - User Guide

## Overview

The Invoice Approval Workflow application is a dynamic rule-based system that automatically routes invoice approval requests to the appropriate approver based on configurable business rules. The system evaluates invoice amount, department, and manager approval requirements to determine the correct approval path.

## Prerequisites

Before running the application, ensure you have the following installed:

| Software | Minimum Version | Recommended |
|----------|-----------------|-------------|
| Java JDK | 17 | 17+ |
| Node.js | 16.x | 18.x or later |
| npm | 8.x | 9.x or later |

To verify your installations:

```bash
java -version
node -v
npm -v
```

## Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd light-challenge
```

### 2. Backend Setup

Navigate to the backend directory and build the project:

```bash
cd backend
./gradlew build
```

On Windows:
```powershell
cd backend
.\gradlew.bat build
```

This will download all dependencies and compile the Kotlin source files.

### 3. Frontend Setup

Navigate to the frontend directory and install dependencies:

```bash
cd frontend
npm install
```

## Running the Application

### Start the Backend Server

From the `backend` directory:

```bash
./gradlew run
```

On Windows:
```powershell
.\gradlew.bat run
```

The backend server starts on **http://localhost:8080**.

### Start the Frontend Development Server

From the `frontend` directory:

```bash
npm run dev
```

The frontend server starts on **http://localhost:3000**.

### Verify Both Services Are Running

1. Backend health check: Open http://localhost:8080/workflow/departments in your browser. You should see a JSON response with available departments.

2. Frontend: Open http://localhost:3000 in your browser. You should see the Invoice Approval Workflow form.

## Using the Application

### Submitting an Invoice Approval Request

1. **Open the application** at http://localhost:3000

2. **Fill in the form fields**:

   | Field | Description | Example |
   |-------|-------------|---------|
   | Invoice Amount (USD) | The monetary value of the invoice | 1500.00 |
   | Department | The department submitting the invoice | ENGINEERING |
   | Requires Manager Approval | Check if manager approval is needed | Checked/Unchecked |

3. **Click "Execute Workflow"** to submit the request

4. **Review the results** displayed below the form:
   - **Rules Applied**: Which workflow rules matched your request
   - **Selected Approvers**: The approver(s) assigned to handle the invoice
   - **Notifications Sent**: Confirmation of Slack notifications sent

### Interpreting Results

After submission, the result panel displays:

- **Amount**: The invoice amount you submitted
- **Department**: The selected department
- **Manager Approval Required**: Yes/No based on your checkbox selection
- **Rules Applied**: List of rule names that matched
- **Selected Approvers**: Approver details including name, email, department, and role
- **Notifications Sent**: List of notification confirmations

## Workflow Logic

The system routes approvals based on two primary factors:

1. **Invoice Amount**: Greater than $1,000 or $1,000 and below
2. **Manager Approval Requirement**: Required or not required

### Approval Routing Table

| Amount | Manager Approval | Approver |
|--------|------------------|----------|
| > $1,000 | Required | CFO |
| > $1,000 | Not Required | VP of Finance |
| <= $1,000 | Required | Department Manager |
| <= $1,000 | Not Required | Employee |

### Workflow Diagram

```
                    +-------------------+
                    | Invoice Submitted |
                    +-------------------+
                            |
                            v
                   +------------------+
                   | Amount > $1,000? |
                   +------------------+
                    /              \
                  Yes               No
                  /                  \
                 v                    v
    +-------------------+    +-------------------+
    | Manager Approval? |    | Manager Approval? |
    +-------------------+    +-------------------+
       /          \            /          \
     Yes          No         Yes          No
      |            |           |            |
      v            v           v            v
   +-----+    +--------+  +----------+  +----------+
   | CFO |    | VP of  |  | Dept     |  | Employee |
   +-----+    | Finance|  | Manager  |  +----------+
              +--------+  +----------+
```

## API Usage

You can interact with the workflow API directly using curl or Postman.

### Endpoint

```
POST http://localhost:8080/workflow/execute
Content-Type: application/json
```

### Request Body

```json
{
  "amount": 1500.00,
  "department": "ENGINEERING",
  "requiresManagerApproval": true
}
```

### Available Departments

`SALES`, `MARKETING`, `ENGINEERING`, `FINANCE`, `HR`, `OPERATIONS`

### Example: Using curl

**High amount with manager approval (routes to CFO):**

```bash
curl -X POST http://localhost:8080/workflow/execute \
  -H "Content-Type: application/json" \
  -d '{"amount": 5000, "department": "SALES", "requiresManagerApproval": true}'
```

**Low amount without manager approval (routes to Employee):**

```bash
curl -X POST http://localhost:8080/workflow/execute \
  -H "Content-Type: application/json" \
  -d '{"amount": 500, "department": "MARKETING", "requiresManagerApproval": false}'
```

### Example Response

```json
{
  "success": true,
  "amount": 5000,
  "department": "SALES",
  "requiresManagerApproval": true,
  "selectedApprovers": [
    {
      "id": "cfo",
      "name": "Chief Financial Officer",
      "email": "cfo@company.com",
      "department": "FINANCE",
      "isManager": true
    }
  ],
  "rulesApplied": [
    "High Amount with Manager Approval"
  ],
  "notificationsSent": [
    "Slack notification sent to Chief Financial Officer"
  ],
  "message": "Workflow executed successfully"
}
```

### Get Available Departments

```bash
curl http://localhost:8080/workflow/departments
```

## Testing

### Running Backend Tests

From the `backend` directory:

```bash
./gradlew test
```

On Windows:
```powershell
.\gradlew.bat test
```

Test reports are generated at `backend/build/reports/tests/test/index.html`.

**Backend test coverage includes:**
- Rule condition evaluation (AmountCondition, DepartmentCondition, ManagerApprovalCondition, AndCondition, OrCondition)
- Notification service message generation
- Workflow engine routing logic
- REST endpoint request/response handling

### Running Frontend Tests

From the `frontend` directory:

```bash
npm test
```

**Frontend test coverage includes:**
- Form rendering and field validation
- User input handling (amount, department, checkbox)
- API call mocking and response handling
- Error state display
- Result panel rendering

### Running All Tests

To run both backend and frontend tests:

```bash
# From project root
cd backend && ./gradlew test && cd ../frontend && npm test
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Backend fails to start | Ensure Java 17+ is installed and `JAVA_HOME` is set correctly |
| Frontend shows connection error | Verify backend is running on port 8080 |
| Port 8080 already in use | Stop other services or change the backend port in configuration |
| Port 3000 already in use | Stop other services or run `npm run dev -- -p 3001` |
| Gradle build fails | Run `./gradlew clean build` to clear cache |

## Support

For issues or questions, please refer to the project README or create an issue in the repository.
