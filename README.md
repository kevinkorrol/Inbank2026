# Inbank2026

Minimal full-stack loan decision engine.

## Tech Stack

- Backend: Java Spring Boot
- Frontend: SvelteKit

## Project Structure

- `backend/` - Spring Boot API and decision engine
- `frontend/` - SvelteKit UI for loan simulation and decision display

## Backend API

### Endpoint

- Method: `POST`
- URL: `/api/loan/decision`
- Example local URL: `http://localhost:8080/api/loan/decision`

### Request Parameters

Request body JSON:

```json
{
	"personalCode": "49002010976",
	"loanAmount": 4000,
	"loanPeriod": 24
}
```

- `personalCode` (string): personal identification code
- `loanAmount` (number): requested loan amount
- `loanPeriod` (number): requested loan period in months

### Response

Response body JSON:

```json
{
	"decision": "POSITIVE",
	"approvedAmount": 6000,
	"approvedPeriod": 60,
	"message": "Loan approved"
}
```

- `decision` (enum): `POSITIVE` or `NEGATIVE`
- `approvedAmount` (number): best approved amount according to rules
- `approvedPeriod` (number): approved period in months
- `message` (string): explanation message

## Backend choices

- Used Maven instead of Gradle.
- Implemented server-side validation.
- Separated backend logic into API and service layers.
- From `credit score = (credit_modifier / loan_amount) * loan_period` and `score >= 1`, it follows that approved `loan_amount <= credit_modifier * loan_period`.
- Kept information in records and separated functions into smaller methods.
- Used enum `Decision` for future scaling and customization options.
- Input validation takes priority over debt checking.
- Added tests that can be run from the backend folder with the command `.\mvnw.cmd test`
- Returns the maximum approvable sum within constraints, adjusting the period when needed. Example: if 3000€ for 30 months and 6000 for 60 months are both valid, return 6000€ for 60 months.

## Frontend choices

- Separated API calls and frontend page logic into separate files.
- Frontend validates personal code as numeric and exactly 11 digits.
- Added tests that can be run from the backend folder with the command `npm run test:decision`
- Added client-side validation, even though sliders constrain values, because users can still type custom values.
- For loans that were not approved, only show returned decision and message.
- Kept the UI minimal, but intuitive. Added Inbank logo and colours to better match a real world application. 

## Running The Application

The project is dockerized, so it can be run with a single command: 

```bash
docker compose up --build
```

After startup:

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`


## What is one thing you would improve about the take home assignment and how would you improve it?

The current requirement to "maximize the sum" is not very user friendly. If a user in Segment 1 (credit_modifier = 100) requests €2000 for 12 months, the engine's "Maximum Sum" logic forces a leap to €6000 for 60 months. This is a 300% increase in debt and a 500% increase in time commitment—conditions a typical short-term borrower would likely reject.

Proposed improvement:
I would improve the engine to provide multiple choices with different conditions. Instead of a single "Global Max" result, the API should return:

- The "Request-Match" Offer: The highest amount possible within the requested period (even if below the €2000 floor, for example €1200 over 12 months, provided the bank is willing to lower that limit).

- The "Minimum-Threshold" Offer: The shortest possible period extension required to reach the €2000 minimum (for example €2000 over 20 months).

- The "Max-Capacity" Offer: The absolute maximum the bank is willing to lend (for example €6000 over 60 months).

- That way, there is a much bigger chance of the client picking one of the options instead of rejecting the offer.

One further optimization would be a 'Shortest-Period' option. If a user qualifies for the maximum cap (€10000) at their requested period, the engine could automatically suggest the shortest possible period that still yields that €10000, saving the customer months of interest while maintaining the maximum loan sum. That means if the customer could get a 10000€ loan for (for example) both a 34 month and a 60 month period, the calculator should include both options and everything in between. Right now, the application only offers the longest period possible.

If the question is asking how to make the take-home assignment more interesting, I would add interest rates and repayment calculations so the decision engine optimizes not only approval amount but also total borrowing cost.

## What I would improve about my application?

For the next steps, I would create custom Exception classes and improve logging to include some information about the requests, but not the clients ID, it would not be smart to log it due to privacy concerns. The application also has no database at the moment, so the approved and denied requests are not saved anywhere. I could also implement load testing to determine the service's maximum throughput and ensure the application remains stable and responsive under high volumes of simultaneous requests
