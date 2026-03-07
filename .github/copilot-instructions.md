Our company is already involved in the car sales business and wants to expand into car rental.
Car rental is a new business line for the company.
We had no prior experience or systems specific to the car rental business.
Use this repository to define documentation and code implementation.
Code design/algorithm, or database design, is allowed.

Purpose
-------
This repository contains documentation and guidance for implementing the Car Rental System. These instructions define the implementation standards, expected working process, and constraints for all teams contributing to this repository.

Scope
-----
- Documentation files live under docs/.
  - docs/prd: Product requirement documents and supporting files.
  - docs/trd: Technical requirement documents and supporting files (authoritative source for technical design decisions).
  - docs/test-scenario: QA-related test scenarios and supporting files.
- This file defines implementation instructions and code standards to be followed by engineering teams working on the car-rental product.

Authoritative Design Reference
------------------------------
- All implementation (code, data schema, architecture) MUST refer to and be consistent with the technical design in the technical requirement document located at: docs/trd.
- When in doubt, follow the TRD and escalate design questions to the system architect or product owner for the car rental line.

What is allowed in this repository
----------------------------------
- This repository is primarily for documentation (PRD, TRD, test scenarios).
- Design artifacts such as architecture diagrams, API contracts, data models, sequence diagrams, algorithms, and database designs are allowed in the docs.
- High-level code design, pseudo-code, and algorithm descriptions are allowed as documentation.
- If the project maintainers want actual implementation code in this repository, this must be coordinated and approved; follow the repo owner’s process before adding source code.

Implementation Standards (for engineering teams)
-----------------------------------------------
When implementing the Car Rental System (in implementation repositories or when adding approved implementation artifacts), follow these standards:

Language, Frameworks, and Runtime
- Use Java 25 for all backend services.
- Use Spring Reactive (Spring WebFlux) for reactive endpoints and asynchronous pipeline.
- Use RESTful APIs for inter-service and external communication.

Database and Persistence
- Use PostgreSQL as the primary relational database.
- Use R2DBC for reactive database connectivity.
- Use Flyway for database initialization and migrations.
  - Store DDL and DML in separate SQL files (migrations).
  - The general rule: one table per SQL file (this includes indices and constraints related to that table).
  - Name and organize migration files clearly and consistently (versioned, with descriptive names).

APIs and Contracts
- Define REST API contracts in the technical docs (OpenAPI/Swagger or equivalent).
- Implementation must strictly follow the API contracts defined in docs/trd.
- Include request/response schemas, error models, authentication/authorization requirements, and example flows in the TRD.

Testing and Quality
- Follow clean code and maintainable design principles.
- Always create unit tests wherever possible.
- Ensure unit tests pass locally before pushing or opening a pull request.
- Include integration and contract tests where appropriate (especially around DB and API contracts).
- Provide instructions in the docs to run the test suite locally and in CI.

Working Process and Repository Hygiene
- Follow a clean code approach: meaningful names, small functions, single responsibility, well-documented public interfaces, and explicit error handling.
- Use conventional commits for commit messages and PR titles (e.g., feat:, fix:, docs:, chore:, test:, refactor:).
- Provide a clear PR description explaining the change, rationale, and any migration steps needed for production.
- Keep pull requests small and focused; prefer multiple small PRs to a single large one.

Database Migration and Data Management
- Use Flyway migrations for schema changes.
- Keep DDL (CREATE TABLE, ALTER TABLE, CREATE INDEX) in migration SQL files; keep DML (seed/reference data) in separate migration files when needed.
- One table per SQL file: each table's DDL and its indices/constraints must be in one dedicated SQL migration file.
- Document data retention, archival, and purging policies in the TRD if applicable.

Security and Secrets
- Do not commit secrets, credentials, or sensitive data to the repository.
- Use environment variables or a secret management solution in deployments and CI.
- Document required environment variables and their purpose in the TRD or deployment docs.

CI/CD and Deployments
- CI pipelines must run unit tests and static checks before allowing merges.
- Ensure DB migrations are executed by the deployment process as a controlled step.
- Document the deployment and rollback process in the TRD or ops runbook.

Contributions and Code in Docs
- Documentation may include design examples, API samples, diagrams, and pseudo-code.
- If actual code examples are included in docs for illustration, mark them clearly as examples (not production code) and avoid including secrets or buildable projects unless explicitly approved.
- If a contributor requests code generation, the maintainers will decide. If the repository is for documentation only, explicit approval is required before adding implementation code.

When to Reject Requests to Generate Code
- If a request is to generate full implementation code within this documentation repository and no approval has been provided, politely refuse and redirect contributors to:
  - Create a request/issue outlining the need and justification, or
  - Propose a separate implementation repository following the organization’s repository-creation policies.

Contacts and Ownership
- Document owners and points of contact (product owner, technical lead, architecture owner) in docs/trd.
- For any technical decision divergence from TRD, get sign-off from the architecture owner.

Appendix: Summary Checklist for Implementers
- [ ] Confirm implementation aligns with docs/trd before writing code.
- [ ] Use Java 25 + Spring Reactive (WebFlux).
- [ ] Use PostgreSQL + R2DBC.
- [ ] Use REST APIs as defined in TRD.
- [ ] Use Flyway; keep DDL and DML separate; one table per SQL file.
- [ ] Follow clean code and write unit tests; ensure tests pass locally.
- [ ] Use conventional commits and document changes in PRs.
- [ ] Do not commit secrets; follow security guidelines.
