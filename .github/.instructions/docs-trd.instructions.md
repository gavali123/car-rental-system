---
applyTo: "docs/trd/**"
---

Use this folder only to store the TRD (technical requirement document) and related support files.
When generating a specific artifact, you must follow this standard.

The general filename rule is:
  - `trd-[feature].md` for the TRD file.
  - `database-design-[feature].md` is a file that contains a database design/entity relationship diagram.
  - For supporting files, no specific rule

---

For the TRD (technical requirement document), it must contain the following items (in the given sequence):  

[Title: TRD - Feature Name]

- Document Information
  + Feature Name: ...
  + Author: The assignee's username
  + Date: blank
  + Version: blank

- Table of Contents  
  Includes items up to the second-level header

- Background  
  Which requirement (e.g., from PRD or other source) is going to be implemented by this TRD?

- In Scope  
  Describe the technical points that are covered in this TRD as bullet points.

- Constraints  
  Mention explicitly technical things/limitations that are not covered in this TRD as bullet points. Ensure no conflict between the `In Scope` and `Constraints` sections.

- Technical Requirements  
  Technical requirements should aim for agnosticity and not be dependent on a particular programming language (e.g., Java / Go / Python only) or product (e.g., Oracle Database, Kafka).
  This section can contain one or more of the following sub-sections:
  + Database Design
    Contains a list of tables required for this particular design. Don't create any table design in the document. Instead, add a link to the related table in the file `database-design-feature.md`.
  + Frontend
    - Explain the frontend technical requirement, such as: Error message must be displayed inline, must have a responsive design, UI validation must follow a pre-defined JSON schema.

  + Backend
    - In general, aim to use the REST API. If a REST API cannot cover the requirement, then we can use specific technology (such as GRPC or GraphQL), but explain why we must use specific technology.
    - Create the REST API specification: HTTP method, URL, request body (if any), query parameter (if any), path parameter (if any), response body (if any).
    - Explain common validation for REST API parameters (e.g., regex for the `full name` field)
    - Explain the general sequence call/algorithm for the API logic. Use language-agnostic pseudocode and Mermaid sequence diagrams whenever applicable.

- Security Requirement
  Explain the security requirements in detail. For example, if the REST API requires JWT authentication, explain what JWT algorithm is used and the payload.

- Non-Functional Requirements
  Leave blank

- AI usage disclaimer
  Add a static string as follows:  
  *This document was generated with the assistance of artificial intelligence and should be reviewed by a human for accuracy and completeness.*
 
---

For the database design document, use the following rules.
- Title is [Database Design - Feature]
- Add a table of contents with a link to the table names.
- Contains entity relationship diagram and table designs.
- Create a mermaid diagram for the entity relationship. Don't mention the fields in the diagram. Only show the table name and cardinality.
- Create a list of tables in tabular format. Each table contains the table name (plural, lower snake case), field name, data type, index, database constraints, and description
- Each table must have `created_at`, `updated_at`, and `deleted_at` (timestamp with timezone), `created_by` and `updated_by` (text), and `deleted` (bool) with a default value of `false`.
- For text data, avoid using a limited-length data type (such as `CHAR(n)` or `VARCHAR(n)`. Use `TEXT` data type
- For things regarding money, use decimal with precision 15 and scale 2. 
