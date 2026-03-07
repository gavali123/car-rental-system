# PRD - Accounting

## Document Information

| Field | Details |
|---|---|
| **Product / Feature Name** | Accounting Module for Car Rental System |
| **Author** | @copilot |
| **Date** | |
| **Version** | |

---

## Table of Contents

1. [Overview](#overview)
2. [Problem Statement](#problem-statement)
3. [Functional Requirements](#functional-requirements)
4. [Non-Functional Requirements](#non-functional-requirements)
5. [Dependency & Constraints](#dependency--constraints)
6. [Success Metrics](#success-metrics)

---

## Overview

### Background

The company is expanding from its core car sales business into a new car rental business line. Unlike car sales — which generate one-time transactional revenue — car rentals produce recurring, time-bound financial obligations that require dedicated accounting capabilities. The organisation has no existing systems or processes designed for rental-specific accounting. To launch and operate the rental business compliantly and profitably, a purpose-built accounting module is required.

### Objective

Provide the accounting team with a reliable, auditable, and integration-ready accounting module within the Car Rental System that automates financial document generation, tax calculation, payment tracking, deposit management, ERP integration, and financial reporting — eliminating the need for manual workarounds and reducing financial risk.

### Goals

- Enable accurate, real-time generation of all rental-related financial documents (invoices, receipts, credit notes, deposit statements).
- Automate tax calculation across supported jurisdictions.
- Provide full audit trails and role-based access controls for all financial transactions.
- Support integration with external accounting/ERP systems.
- Deliver actionable financial reports and KPIs to support business decisions.
- Ensure all processes meet regulatory and compliance requirements for the rental industry.

---

## Problem Statement

The accounting team currently has no tools or workflows designed for the car rental business. When a customer rents a vehicle, the team must be able to:

- Issue correct invoices and receipts immediately.
- Record and settle security deposits accurately.
- Calculate the right taxes for every transaction.
- Handle refunds, disputes, and damage charges in a controlled, auditable manner.
- Export financial data to the company's existing ERP or accounting system.
- Produce management reports on revenue, receivables, and operational performance.

Without a dedicated accounting module, the team will face manual data entry, risk of errors, compliance failures, and an inability to reconcile payments efficiently.

**Key Users Affected:**
- **Accountants / Finance staff** — responsible for invoicing, reconciliation, and record keeping.
- **Finance Manager / Controller** — approves financial adjustments, monitors KPIs.
- **Auditors (internal/external)** — require complete and tamper-evident audit trails.
- **Operations staff** — raise damage charges; record rental outcomes.
- **Corporate customers** — require periodic consolidated billing and statements.

**Why This Is Important Now:**  
The rental business line is launching imminently. Without accounting capabilities from day one, the company risks financial misstatement, regulatory non-compliance, and poor customer experience (e.g., incorrect invoices, delayed refunds, unresolved disputes).

---

## Functional Requirements

---

### FR-01: Financial Document Generation

**Title:** Generate Rental Financial Documents

**Statement:**  
**As an** accountant, **I want** the system to automatically generate invoices, receipts, credit notes, and deposit statements for every rental transaction, **so that** I have accurate, ready-to-issue financial documents without manual preparation.

**Requirement Detail:**  
The system must produce the following document types:
- **Invoice** — issued to the customer at the point of rental confirmation or upon checkout; itemises all charges.
- **Receipt** — issued upon successful payment capture.
- **Credit Note** — issued when a full or partial refund is approved.
- **Deposit Statement** — issued when a security deposit is collected, held, adjusted, or released.

Each document must carry a unique sequential reference number, timestamps, customer details, rental reference, and a complete line-item breakdown.

**Acceptance Criteria:**

| # | Given | When | Then |
|---|---|---|---|
| 1 | A rental booking is confirmed | The system finalises the booking | An invoice is automatically generated and available for the accountant to review and send. |
| 2 | A payment is successfully captured | The payment is recorded in the system | A receipt is automatically generated and linked to the invoice. |
| 3 | A refund is approved | The approval is recorded | A credit note is automatically generated referencing the original invoice. |
| 4 | A security deposit is collected | The deposit transaction is processed | A deposit statement is generated and linked to the rental record. |
| 5 | Any financial document is generated | — | It carries a unique reference number, creation timestamp, and full line-item detail. |

---

### FR-02: Invoicing Model

**Title:** Support Multiple Invoicing Models

**Statement:**  
**As an** accountant, **I want** the system to support per-rental invoicing for individual customers and periodic consolidated billing for corporate accounts, **so that** I can meet the billing expectations of different customer segments.

**Requirement Detail:**  
- **Per-rental invoice:** One invoice is generated per rental transaction, issued at the time of checkout or rental confirmation.
- **Periodic/consolidated billing:** For corporate accounts, the system must accumulate all rentals within a billing period (e.g., monthly) and generate a single consolidated invoice.
- **Pro-rated charges:** When a rental is extended, shortened, or starts/ends mid-period, the system must calculate charges on a pro-rated basis (by day, half-day, or hour as configured).
- The invoicing model for a given customer or account must be configurable by an authorised user.

**Acceptance Criteria:**

| # | Given | When | Then |
|---|---|---|---|
| 1 | An individual customer completes a rental | Checkout is processed | A single per-rental invoice is generated. |
| 2 | A corporate account is configured for monthly billing | The billing period closes | A consolidated invoice covering all rentals in that period is generated. |
| 3 | A rental is extended beyond its original end date | The extension is confirmed | The additional charge is calculated on a pro-rated basis and added to the invoice. |
| 4 | A rental ends earlier than booked | Early return is recorded | A pro-rated credit or reduced charge is reflected on the final invoice per the configured policy. |

---

### FR-03: Revenue Recognition for Multi-Period Rentals

**Title:** Handle Revenue Recognition Across Accounting Periods

**Statement:**  
**As a** finance manager, **I want** rental revenue to be recognised correctly when a rental spans two or more accounting periods, **so that** our financial statements accurately reflect income earned in each period.

**Requirement Detail:**  
- When a rental spans an accounting period boundary (e.g., month-end), the system must split the revenue proportionally across the relevant periods.
- Deferred revenue must be recorded for the portion of a prepaid rental that falls in a future period.
- The system must produce a revenue recognition schedule for any rental spanning multiple periods.
- Accounting entries must align with the configured chart of accounts.

**Acceptance Criteria:**

| # | Given | When | Then |
|---|---|---|---|
| 1 | A rental starts on 25 March and ends on 5 April | The rental is invoiced | Revenue is split: the portion for 25–31 March is recognised in March; the portion for 1–5 April is recognised in April. |
| 2 | A customer prepays for a 60-day rental | Payment is captured | Deferred revenue is recorded at payment date; revenue is released daily over the rental period. |
| 3 | A rental spans multiple periods | Period-end close is run | The system produces a revenue recognition schedule showing amounts attributed to each period. |

---

### FR-04: Automated Tax Calculation

**Title:** Automate Tax Calculation for Rental Transactions

**Statement:**  
**As an** accountant, **I want** the system to automatically calculate the correct taxes for each rental transaction based on the applicable jurisdiction and configured rates, **so that** I do not have to calculate taxes manually and can be confident in compliance.

**Requirement Detail:**  
- The system must support configuration of multiple tax jurisdictions (e.g., by country, state/province, or city).
- Tax rates (e.g., VAT, GST, local surcharges) must be configurable per jurisdiction and per charge type.
- Tax must be applied to all taxable line items: base rental rate, add-ons, and fees (as configured).
- Tax-exempt transactions (e.g., for qualifying corporate customers) must be supported with appropriate documentation requirements.
- Tax amounts must be itemised separately on every invoice and receipt.

**Acceptance Criteria:**

| # | Given | When | Then |
|---|---|---|---|
| 1 | A rental occurs in a configured tax jurisdiction | An invoice is generated | The correct tax rate is applied automatically and the tax amount is shown as a separate line item. |
| 2 | A rental location spans two jurisdictions | The rental location is set | The system applies the jurisdiction-specific tax rules for that location. |
| 3 | A corporate customer is marked tax-exempt | An invoice is generated | No tax is charged and a tax-exemption reference is recorded on the invoice. |
| 4 | A tax rate changes | An administrator updates the rate in the system | All new transactions from the effective date use the updated rate; historical invoices remain unchanged. |

---

### FR-05: Transaction Line-Item Detail

**Title:** Capture Full Line-Item Detail for Every Rental Transaction

**Statement:**  
**As an** accountant, **I want** each rental transaction to include a complete, itemised breakdown of all charges, **so that** I can reconcile, audit, and report on every component of revenue.

**Requirement Detail:**  
Every rental transaction record must include separate line items for:
- Base rental rate (daily/hourly rate × duration)
- Applicable taxes (per jurisdiction)
- Mandatory fees (e.g., airport surcharge, young driver fee)
- Optional add-ons (e.g., GPS, child seat, insurance)
- Applied discounts or promotions (with reference codes)
- Damage/penalty charges
- Security deposit (shown separately, not as revenue)
- Any adjustments or credit notes applied

**Acceptance Criteria:**

| # | Given | When | Then |
|---|---|---|---|
| 1 | A rental includes a base rate, an add-on, a discount, and a tax | The invoice is generated | Each item appears as a separate labelled line item with its own amount. |
| 2 | A damage penalty is applied after rental completion | The charge is recorded | A new line item appears on the updated invoice referencing the damage report. |
| 3 | A discount code is applied | The booking is confirmed | The discount is shown as a negative line item with the discount code referenced. |

---

### FR-06: Security Deposit Management

**Title:** Record and Settle Security Deposits

**Statement:**  
**As an** accountant, **I want** security deposits to be recorded as holds or pre-authorisations and settled or released at rental completion, **so that** deposits are never incorrectly recognised as revenue and customers are refunded correctly.

**Requirement Detail:**  
- The system must support two deposit modes: **pre-authorisation (hold)** and **captured amount**.
- Deposits must be recorded in a dedicated liability account (not revenue).
- At rental completion, the system must:
  - Release the full hold if no damage is found.
  - Capture the required amount for damages and release the remainder.
  - Record the full capture if the pre-authorisation was already captured.
- Deposit transactions must appear on the deposit statement and be excluded from revenue reports.

**Acceptance Criteria:**

| # | Given | When | Then |
|---|---|---|---|
| 1 | A security deposit is collected as a pre-authorisation | Rental starts | The deposit is recorded as a liability (hold), not as revenue. |
| 2 | A rental is completed with no damage | Rental is closed | The full deposit hold is released and the customer's deposit statement reflects the release. |
| 3 | Damage is found at return | A damage charge is recorded | The deposit is partially captured to cover the damage amount; the remainder is released; both actions appear on the deposit statement. |
| 4 | The deposit was captured (not held) | Rental is closed with no damage | A refund equal to the deposit is initiated and a credit note is issued. |

---

### FR-07: Refund, Adjustment, and Dispute Workflows

**Title:** Manage Refunds, Adjustments, and Disputes with Approval Controls

**Statement:**  
**As an** accountant, **I want** a structured workflow for processing refunds, financial adjustments, and customer disputes, **so that** no financial change is made without the appropriate approval and a complete record is kept.

**Requirement Detail:**  
- Refunds above a configurable threshold require approval from a Finance Manager before processing.
- Financial adjustments (e.g., invoice corrections, write-offs) must be initiated by accounting staff and approved by the Finance Manager.
- Dispute records must capture: date raised, reason, supporting evidence, resolution steps, and final outcome.
- All workflow state transitions (submitted, under review, approved, rejected, processed) must be timestamped and attributed to the acting user.
- Approved refunds must automatically generate a credit note and trigger the refund payment process.

**Acceptance Criteria:**

| # | Given | When | Then |
|---|---|---|---|
| 1 | A refund request is submitted above the approval threshold | The request is raised | The system routes it to the Finance Manager for approval before any funds are moved. |
| 2 | A Finance Manager approves a refund | Approval is recorded | The system automatically generates a credit note and initiates the refund to the customer's payment method. |
| 3 | A Finance Manager rejects an adjustment | Rejection is recorded | The accountant is notified; no financial change is made; the rejection reason is stored. |
| 4 | A customer dispute is logged | Dispute is created | All subsequent actions (investigation notes, approvals, resolution) are captured against the dispute record. |

---

### FR-08: Accounting / ERP System Integration

**Title:** Integrate with External Accounting and ERP Systems

**Statement:**  
**As a** finance manager, **I want** the car rental accounting module to integrate with the company's accounting or ERP system, **so that** rental financial data flows automatically into our general ledger without manual re-keying.

**Requirement Detail:**  
- The system must support outbound data export to one or more accounting/ERP platforms (specific platforms to be confirmed during technical discovery).
- Integration must map rental transactions to the company's chart of accounts and GL codes.
- Failed integration events must be logged and retried; persistent failures must alert the finance team.
- All outbound data transfers must be idempotent (duplicate transmissions must not create duplicate ledger entries).
- Integration credentials and connection configuration must be stored securely.

**Acceptance Criteria:**

| # | Given | When | Then |
|---|---|---|---|
| 1 | A financial transaction is finalised in the rental system | The integration job runs | The transaction is exported to the ERP with the correct GL mapping. |
| 2 | An export fails due to a connectivity issue | The failure is detected | The system logs the failure, retries automatically, and alerts the finance team if retries are exhausted. |
| 3 | The same transaction is submitted twice to the ERP | Duplicate submission occurs | The ERP receives only one entry; no duplicate is created. |

---

### FR-09: Export Formats and Frequencies

**Title:** Configure Financial Data Export Formats and Schedules

**Statement:**  
**As an** accountant, **I want** to export financial data in standard formats on a defined schedule, **so that** our accounting system receives timely and correctly formatted transaction files.

**Requirement Detail:**  
- The system must support at least the following export formats: CSV, JSON, and industry-standard journal entry files (e.g., flat file as required by the target ERP).
- Export schedules must be configurable: daily batch (end-of-day), on-demand, or real-time (event-driven).
- Each export file must include: transaction date, transaction type, GL account code, debit/credit amounts, currency, rental reference, and customer reference.
- Exported files must be stored securely and available for re-download for at least the duration of the retention period.

**Acceptance Criteria:**

| # | Given | When | Then |
|---|---|---|---|
| 1 | The end-of-day batch job runs | Daily schedule triggers | A journal entry export file containing all transactions from that day is generated in the configured format and delivered to the accounting system. |
| 2 | An accountant requests an on-demand export | Export is initiated | The system generates and delivers the file within an agreed SLA. |
| 3 | An export file is generated | File is created | It contains all required fields: transaction date, type, GL code, debit/credit, currency, rental and customer references. |

---

### FR-10: Chart of Accounts and GL Mapping

**Title:** Configure and Map Chart of Accounts

**Statement:**  
**As a** finance manager, **I want** to configure the chart of accounts and define mapping rules between rental transaction types and GL account codes, **so that** all financial entries are posted to the correct accounts automatically.

**Requirement Detail:**  
- Authorised users must be able to create, edit, and deactivate GL account codes in the system.
- Mapping rules must allow each transaction type (e.g., base rental revenue, tax collected, deposit liability, damage fee) to be mapped to a specific GL account.
- The system must validate that every transaction type has a valid GL mapping before an export is generated.
- Mapping changes must be versioned and take effect from a configurable date.

**Acceptance Criteria:**

| # | Given | When | Then |
|---|---|---|---|
| 1 | A finance manager creates a new GL code | The code is saved | It becomes available for mapping to transaction types. |
| 2 | A transaction type is mapped to a GL code | The mapping is saved | All future transactions of that type are exported with the mapped GL code. |
| 3 | An export is attempted with an unmapped transaction type | Export job runs | The system blocks the export and alerts the finance team to complete the mapping. |
| 4 | A GL mapping is updated with a future effective date | The date is reached | Transactions from that date onward use the new mapping; prior transactions retain the original mapping. |

---

### FR-11: Multi-Currency Support

**Title:** Handle Multi-Currency Transactions

**Statement:**  
**As an** accountant, **I want** the system to support transactions in multiple currencies with configurable exchange rates, **so that** international rentals are correctly recorded and revalued in our base (settlement) currency.

**Requirement Detail:**  
- Each transaction must record both the transaction currency and the equivalent amount in the base/settlement currency.
- Exchange rates must be configurable (manual entry or automatic feed from a configured source) and dated.
- At period end, open foreign-currency balances (e.g., outstanding invoices) must be revalued using the closing exchange rate.
- Realised and unrealised foreign-exchange gains and losses must be calculated and posted to the appropriate GL accounts.

**Acceptance Criteria:**

| # | Given | When | Then |
|---|---|---|---|
| 1 | A rental is invoiced in a foreign currency | The invoice is generated | The system records both the foreign-currency amount and the base-currency equivalent using the rate current at invoice date. |
| 2 | Period-end revaluation is run | The revaluation job executes | Outstanding foreign-currency balances are restated at the closing rate; the difference is posted as an unrealised FX gain or loss. |
| 3 | A foreign-currency invoice is settled | Payment is received | The realised FX gain or loss (difference between invoice rate and settlement rate) is calculated and posted to the FX account. |

---

### FR-12: Audit Trail for Financial Events

**Title:** Maintain a Complete Audit Trail for All Financial Events

**Statement:**  
**As an** auditor, **I want** every financial event in the system to be logged with full details of who made the change, when, and what changed, **so that** I can trace any transaction back to its origin and demonstrate compliance.

**Requirement Detail:**  
- Every create, update, or delete action on financial records (invoices, payments, deposits, adjustments, GL mappings) must be logged.
- Each log entry must record: timestamp (UTC), user identity, action type, affected record ID, before-value, and after-value.
- Audit logs must be immutable — no user, including administrators, may alter or delete audit entries.
- Audit logs must be searchable by date range, user, transaction type, and record ID.

**Acceptance Criteria:**

| # | Given | When | Then |
|---|---|---|---|
| 1 | An accountant modifies an invoice | The change is saved | An audit log entry is created recording the user, timestamp, and the before/after values of every changed field. |
| 2 | An administrator attempts to delete an audit log entry | The deletion is attempted | The system prevents the deletion and records the attempt. |
| 3 | An auditor searches for all changes by a specific user in a date range | The search is executed | All matching audit entries are returned in chronological order. |

---

### FR-13: Financial Record Retention

**Title:** Enforce Retention Policy for Financial Records

**Statement:**  
**As a** finance manager, **I want** financial records and transaction logs to be retained for a defined minimum period, **so that** the company meets its legal and regulatory obligations.

**Requirement Detail:**  
- The retention period for financial records (invoices, receipts, payments, audit logs) must be configurable per record type and jurisdiction (minimum 7 years unless overridden by local regulation).
- Records within the retention period must be protected from deletion.
- Once the retention period expires, the system must flag records for archival or deletion, subject to a final authorised approval.
- Retained records must remain accessible and searchable throughout the retention period.

**Acceptance Criteria:**

| # | Given | When | Then |
|---|---|---|---|
| 1 | A user attempts to delete an invoice within its retention period | Deletion is requested | The system blocks the deletion and displays the retention expiry date. |
| 2 | A record reaches the end of its retention period | The retention date passes | The system flags the record for archival/deletion review by an authorised user. |
| 3 | An auditor searches for a 5-year-old invoice | Search is executed | The invoice and all associated records are returned in full. |

---

### FR-14: Payment and Deposit Reconciliation

**Title:** Perform Payment, Deposit, and Refund Reconciliation

**Statement:**  
**As an** accountant, **I want** the system to match payments, deposits, and refunds against the corresponding invoices and expected amounts, **so that** I can quickly identify and investigate any discrepancies.

**Requirement Detail:**  
- The system must automatically match incoming payments to open invoices.
- Unmatched or partially matched payments must be flagged for manual review.
- Deposit releases and captures must be reconciled against the original deposit record.
- Refund transactions must be reconciled against the originating credit note.
- A daily reconciliation report must summarise: matched items, unmatched items, total amounts by type, and outstanding balances.

**Acceptance Criteria:**

| # | Given | When | Then |
|---|---|---|---|
| 1 | A payment is received that exactly matches an open invoice | Payment is recorded | The invoice is automatically marked as paid and the match is recorded. |
| 2 | A payment is received that does not match any open invoice | Payment is recorded | The payment is flagged as unmatched and appears in the reconciliation exception report. |
| 3 | The daily reconciliation job runs | End of day | A reconciliation report is generated showing all matched, unmatched, and partially matched items. |

---

### FR-15: Financial Reporting and KPIs

**Title:** Provide Financial Reports and KPI Dashboard

**Statement:**  
**As a** finance manager, **I want** access to standard financial reports and a KPI dashboard, **so that** I can monitor the rental business's financial performance and make informed decisions.

**Requirement Detail:**  
The system must provide at minimum the following reports, filterable by date range, location, vehicle category, and customer type:
- **Revenue report** — total revenue broken down by location, vehicle type, and customer segment.
- **Aging receivables report** — outstanding invoices grouped by age (0–30, 31–60, 61–90, 90+ days).
- **Occupancy/utilisation report** — vehicle utilisation rate and associated revenue per vehicle per period.
- **Tax collected report** — tax amounts by jurisdiction and type.
- **Deposit status report** — open deposits, released deposits, captured amounts.
- **Refunds and adjustments report** — all credit notes and adjustments within a period.

KPIs to be displayed on a dashboard: total rental revenue, average revenue per rental, outstanding receivables, refund rate, deposit capture rate.

**Acceptance Criteria:**

| # | Given | When | Then |
|---|---|---|---|
| 1 | A finance manager opens the revenue report | Filters are applied | The report displays revenue broken down by the selected dimensions, downloadable as CSV or PDF. |
| 2 | An invoice becomes overdue by 31 days | The aging report is generated | The invoice appears in the 31–60 days aging bucket. |
| 3 | A finance manager opens the KPI dashboard | Dashboard loads | All configured KPIs are displayed with current and prior-period values. |

---

### FR-16: Access Controls and Approval Authority

**Title:** Enforce Role-Based Access Controls for Financial Operations

**Statement:**  
**As a** finance manager, **I want** all financial operations to be governed by role-based access controls and approval rules, **so that** no unauthorised changes can be made to financial records.

**Requirement Detail:**  
- Define at least the following roles with differentiated permissions: **Accountant**, **Finance Manager**, **Auditor (read-only)**, **System Administrator**.
- Sensitive operations (invoice void, credit note above threshold, GL mapping change, bulk write-off) must require Finance Manager approval.
- All access attempts (including denied ones) must be logged.
- Permission changes must require a separate authorised approver and must be audit-logged.

**Acceptance Criteria:**

| # | Given | When | Then |
|---|---|---|---|
| 1 | An accountant attempts to approve a large refund | Action is attempted | The system prevents the action and prompts escalation to the Finance Manager. |
| 2 | A Finance Manager approves a void invoice | Approval is given | The void is applied; the action and approver are recorded in the audit trail. |
| 3 | An auditor attempts to edit any financial record | Edit is attempted | The system blocks the edit; the attempt is logged. |

---

### FR-17: Damage and Repair Charge Capture

**Title:** Capture and Invoice Damage and Repair Charges Post-Rental

**Statement:**  
**As an** accountant, **I want** to record damage or repair charges raised by operations after a rental is returned, and have these automatically invoiced to the customer, **so that** all post-rental charges are correctly billed and recovered.

**Requirement Detail:**  
- Operations staff must be able to create a damage report for a returned vehicle, attaching supporting evidence (photos, inspection notes).
- Once the damage report is approved by an authorised user, the system must automatically calculate the applicable charge (based on a configured damage rate schedule or manual amount).
- An updated or supplementary invoice must be generated and issued to the customer.
- If a security deposit was held, it must be applied against the damage charge; any shortfall must be separately invoiced.
- All damage charges must be traceable to the originating damage report.

**Acceptance Criteria:**

| # | Given | When | Then |
|---|---|---|---|
| 1 | Operations staff create a damage report for a returned vehicle | Report is submitted | The damage report is routed for authorised approval before any charge is created. |
| 2 | A damage charge is approved | Approval is recorded | The system generates a supplementary invoice for the damage amount and notifies the customer. |
| 3 | A security deposit covers part of the damage charge | The charge is processed | The deposit is applied first; a separate invoice is raised for the remaining balance. |
| 4 | An accountant views a damage charge | Record is opened | The originating damage report and all approval steps are visible and linked. |

---

### FR-18: Service Level Agreements for Financial Data

**Title:** Meet SLAs for Financial Data Availability and Batch Processing

**Statement:**  
**As a** finance manager, **I want** the system to meet defined SLAs for financial data availability, report generation, and end-of-day batch processing, **so that** the team can rely on timely data for operations and decision-making.

**Requirement Detail:**  
- End-of-day (EOD) batch jobs (reconciliation, export, revenue recognition) must complete within a configurable window (default: within 2 hours of the configured EOD cut-off time).
- Financial reports must be available within a configurable maximum response time (default: 30 seconds for standard reports, 5 minutes for large date-range reports).
- The system must notify the finance team if any batch job exceeds its SLA window.
- SLA configuration must be accessible to the System Administrator.

**Acceptance Criteria:**

| # | Given | When | Then |
|---|---|---|---|
| 1 | The EOD batch job is triggered | Batch starts | The job completes within the configured SLA window. |
| 2 | A batch job exceeds its SLA window | SLA breach is detected | The system sends an automated alert to the finance team with the job name and elapsed time. |
| 3 | A standard financial report is requested | Request is submitted | The report is returned within 30 seconds under normal system load. |

---

### FR-19: Regulatory and Compliance Reporting

**Title:** Support Regulatory and Compliance Reporting for the Rental Business

**Statement:**  
**As a** finance manager, **I want** the system to support regulatory reporting requirements applicable to the car rental business in each operating jurisdiction, **so that** the company meets its legal financial reporting obligations.

**Requirement Detail:**  
- The system must be configurable to meet jurisdiction-specific financial reporting requirements (e.g., VAT/GST returns, withholding tax reports).
- Reports required for regulatory submission must be exportable in the format required by the relevant authority (e.g., XML, CSV, specific government portal format).
- All regulatory data (tax collected, tax-exempt transactions, cross-border transactions) must be traceable to the underlying rental transactions.
- The system must flag any transaction that lacks the data required for regulatory reporting.

**Acceptance Criteria:**

| # | Given | When | Then |
|---|---|---|---|
| 1 | A VAT return period closes | The report is generated | The system produces a VAT/GST report reconciling tax collected, tax paid, and net liability for the period. |
| 2 | A transaction is missing a required regulatory data field | Transaction is finalised | The system flags the transaction and prevents it from being included in a regulatory export until the gap is resolved. |
| 3 | A regulatory report is exported | Export is triggered | The file is produced in the format required by the relevant tax authority. |

---

### FR-20: MVP Accounting Features

**Title:** Define and Deliver MVP Accounting Capabilities

**Statement:**  
**As a** product owner, **I want** a clearly defined set of minimum viable accounting features to be delivered at launch, **so that** the rental business can operate legally and efficiently from day one without waiting for full feature completeness.

**Requirement Detail:**  
The MVP scope for accounting must include:
1. **Invoice and receipt generation** — per-rental invoices and receipts issued automatically.
2. **Basic tax calculation** — at least one configured tax jurisdiction with a fixed tax rate.
3. **Payment recording** — ability to record and reconcile customer payments against invoices.
4. **Security deposit recording** — hold and release of security deposits as a liability (not revenue).
5. **Basic export to ERP** — daily batch export of journal entries in the agreed format.
6. **Audit trail** — logging of all create/update actions on financial records.
7. **Role-based access** — Accountant and Finance Manager roles with appropriate permissions.
8. **Basic financial reports** — revenue report and outstanding receivables report.

Features deferred post-MVP: multi-currency, periodic corporate billing, automated regulatory reporting, advanced reconciliation, KPI dashboard, damage invoice automation.

**Acceptance Criteria:**

| # | Given | When | Then |
|---|---|---|---|
| 1 | A rental is completed | Checkout is processed | An invoice and receipt are automatically generated. |
| 2 | A payment is received | Payment is recorded | It is matched to the open invoice and the invoice is marked as paid. |
| 3 | The EOD batch runs on the first day of operation | Batch executes | A journal entry export file is delivered to the ERP system. |
| 4 | All MVP features are present | UAT is conducted | All acceptance criteria in FR-01, FR-04, FR-06, FR-12, FR-14 (basic), FR-16 are satisfied. |

---

## Non-Functional Requirements

*(To be defined in a subsequent iteration in collaboration with the engineering and security teams.)*

---

## Dependency & Constraints

- **ERP / Accounting System:** The specific accounting or ERP system(s) to be integrated have not yet been confirmed. Integration design in FR-08 and FR-09 is subject to change once the target system is identified.
- **Tax Jurisdiction Scope:** At launch, only the primary operating jurisdiction's tax rules will be configured. Multi-jurisdiction support (FR-04) will be available but initially configured for one jurisdiction only.
- **Currency Scope:** Multi-currency support (FR-11) is deferred post-MVP. MVP will operate in a single base currency.
- **Periodic Corporate Billing:** Consolidated periodic billing for corporate accounts (FR-02) is deferred post-MVP.
- **Desktop Web Only:** The accounting module is scoped for desktop web browser access at launch. Mobile and native app access are out of scope for the initial release.
- **Damage Charge Automation:** Full automation of damage invoicing (FR-17) requires the vehicle inspection module to be delivered first. Manual damage charge entry will be the MVP fallback.
- **Regulatory Reporting:** Automated regulatory export (FR-19) is deferred post-MVP. Initial compliance will be supported through manual extraction of underlying data.

---

## Success Metrics

- **Invoice Accuracy Rate:** ≥ 99.5% of invoices generated without manual correction within 30 days of launch.
- **Reconciliation Rate:** ≥ 95% of daily payments automatically matched to invoices (no manual intervention required) within 60 days of launch.
- **EOD Batch Completion:** 100% of EOD batch jobs completed within the configured SLA window.
- **Audit Trail Completeness:** 100% of financial events captured in the audit log; zero unlogged transactions detected in internal audit.
- **ERP Export Success Rate:** ≥ 99% of daily export jobs completed successfully without manual re-submission.
- **Time to Issue Invoice:** Average time from rental checkout to invoice delivery reduced to < 1 minute (from current manual process).
- **Dispute Resolution Time:** Average financial dispute resolved within 5 business days of logging.
