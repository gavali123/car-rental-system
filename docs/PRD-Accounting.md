# Product Requirement Document — Accounting Role
## Car Rental System

**Version:** 1.0  
**Status:** Draft  
**Date:** 2026-03-04  
**Prepared by:** Product Team  

---

## Table of Contents

1. [Overview](#1-overview)  
2. [Goals and Objectives](#2-goals-and-objectives)  
3. [Stakeholders](#3-stakeholders)  
4. [Functional Requirements](#4-functional-requirements)  
   4.1 [Financial Documents and Records](#41-financial-documents-and-records)  
   4.2 [Invoicing Model](#42-invoicing-model)  
   4.3 [Revenue Recognition](#43-revenue-recognition)  
   4.4 [Tax Calculation](#44-tax-calculation)  
   4.5 [Transaction Detail](#45-transaction-detail)  
   4.6 [Security Deposits](#46-security-deposits)  
   4.7 [Refunds, Adjustments, and Disputes](#47-refunds-adjustments-and-disputes)  
   4.8 [Accounting and ERP Integration](#48-accounting-and-erp-integration)  
   4.9 [Export Formats and Frequencies](#49-export-formats-and-frequencies)  
   4.10 [Chart of Accounts and Mapping](#410-chart-of-accounts-and-mapping)  
   4.11 [Multi-Currency Support](#411-multi-currency-support)  
   4.12 [Audit Trail](#412-audit-trail)  
   4.13 [Record Retention](#413-record-retention)  
   4.14 [Reconciliation Processes](#414-reconciliation-processes)  
   4.15 [Reporting and Financial KPIs](#415-reporting-and-financial-kpis)  
   4.16 [Access Controls and Authorization](#416-access-controls-and-authorization)  
   4.17 [Damage and Repair Charges](#417-damage-and-repair-charges)  
   4.18 [SLAs for Financial Data](#418-slas-for-financial-data)  
   4.19 [Regulatory and Compliance Constraints](#419-regulatory-and-compliance-constraints)  
5. [MVP Acceptance Criteria](#5-mvp-acceptance-criteria)  
6. [Non-Functional Requirements](#6-non-functional-requirements)  
7. [Engineering and Operations Considerations](#7-engineering-and-operations-considerations)  
8. [Out of Scope](#8-out-of-scope)  
9. [Glossary](#9-glossary)  

---

## 1. Overview

This document defines the product requirements for the **Accounting** role within the Car Rental System. The accounting module must handle all financial events related to vehicle rentals — from initial booking through final settlement — while providing reliable audit trails, regulatory compliance, and seamless integration with external accounting and ERP systems.

These requirements were gathered through a structured set of preliminary interview questions with accounting stakeholders and engineering/operations leads.

---

## 2. Goals and Objectives

| # | Goal | Success Metric |
|---|------|----------------|
| G1 | Produce accurate and timely financial documents | 100% of completed rentals generate a closed invoice within 24 hours |
| G2 | Automate tax calculation across all required jurisdictions | Zero manual tax-entry errors; tax rates applied correctly per jurisdiction |
| G3 | Maintain a tamper-evident audit trail for all financial events | All financial state changes logged with actor, timestamp, and before/after values |
| G4 | Support end-of-day (EOD) reconciliation and ERP export | EOD batch job completes within 2 hours of business day close |
| G5 | Enforce role-based access controls over financial data | No unauthorized financial changes recorded in any audit period |

---

## 3. Stakeholders

| Role | Responsibility |
|------|---------------|
| Accounting Manager | Owns chart-of-accounts, approves adjustments, reviews financial reports |
| Accounts Receivable Clerk | Processes invoices, handles refunds and disputes |
| Finance Controller | Oversees multi-currency revaluation, regulatory compliance |
| IT / Integration Engineer | Maintains ERP connectors, batch export pipelines |
| Auditor (internal/external) | Reviews audit trails, retention compliance |
| Customer | Receives invoices, receipts, credit notes |

---

## 4. Functional Requirements

### 4.1 Financial Documents and Records

The system must generate and store the following financial documents for every rental transaction:

| Document | Trigger | Required Fields |
|----------|---------|-----------------|
| **Invoice** | Rental confirmation or checkout | Invoice number, customer details, rental period, line items, tax breakdown, total amount due |
| **Receipt** | Payment captured | Payment method, amount, date/time, reference number |
| **Credit Note** | Refund or billing adjustment approved | Original invoice reference, reason, adjusted amount |
| **Deposit Statement** | Security deposit held or released | Deposit amount, hold/capture status, release date, settlement amount |
| **Damage/Repair Invoice** | Damage assessment completed post-rental | Description of damage, repair cost, deduction from deposit |

All documents must be available in PDF and structured data (JSON/XML) formats and must be retrievable for the full retention period (see §4.13).

---

### 4.2 Invoicing Model

The system must support the following invoicing models:

1. **Per-Rental Invoice** — a single invoice generated per individual rental, covering the full rental period, applicable taxes, fees, add-ons, and penalties.
2. **Periodic Billing for Corporate Accounts** — for B2B customers with active corporate agreements, invoices may be consolidated and issued on a configurable cycle (weekly, bi-weekly, or monthly).
3. **Pro-Rated Charges** — when a rental is extended, returned early, or starts/ends mid-period, charges must be calculated on a pro-rated daily or hourly basis using the rate applicable at time of booking confirmation.

**Business Rules:**
- A rental must be associated with exactly one invoicing model, determined at booking creation.
- Invoice line items must be itemized (base rate, taxes, fees, discounts, add-ons, penalties) so that the accounting team can reconcile each component.
- Voided invoices must retain a record and reference the replacement document.

---

### 4.3 Revenue Recognition

Revenue must be recognized in accordance with accrual accounting principles:

| Scenario | Recognition Rule |
|----------|-----------------|
| Rental within one accounting period | Full rental revenue recognized on rental close date |
| Rental spanning two or more accounting periods | Revenue split and recognized proportionally across each period (daily proration) |
| Advance payment / prepaid rental | Deferred to a "Deferred Revenue" liability account until the rental period is earned |
| Cancellation with partial refund | Recognize earned portion; reverse unearned portion to original revenue account |

**Requirements:**
- The system must store the rental start date, end date, and payment date independently to support period-end calculations.
- A period-end routine must generate journal entries to move revenue between "Deferred Revenue" and the applicable revenue accounts.
- The accounting team must be able to preview period-end entries before posting.

---

### 4.4 Tax Calculation

The system must support automated tax calculation with the following capabilities:

- **Jurisdiction support:** Federal, state/province, county, and city-level tax rules configurable per rental location.
- **Tax rate management:** A configurable tax-rate table with effective date ranges so that rate changes do not retroactively affect closed invoices.
- **Tax types supported:** Sales tax, VAT, GST, tourism/local surcharge, environmental levies — configurable per jurisdiction.
- **Exemptions:** Corporate or government customers may carry a tax-exempt status that suppresses applicable tax lines.
- **Rounding rules:** Configurable per jurisdiction (half-up, banker's rounding).
- **Tax display:** Tax amounts must be shown as separate line items on invoices, distinguishable by tax type and jurisdiction.

**Non-functional:** Tax engine must process calculation within 500 ms per transaction.

---

### 4.5 Transaction Detail

Every financial transaction record must capture the following fields:

| Field | Description |
|-------|-------------|
| Transaction ID | System-generated unique identifier |
| Rental ID | Reference to parent rental |
| Customer ID | Reference to customer record |
| Base Rental Rate | Daily/hourly rate × duration |
| Duration | Confirmed rental period (start/end timestamps) |
| Add-ons | Itemized list of optional extras (GPS, child seat, insurance, etc.) |
| Discounts | Applied discount codes or corporate rates, with original rate preserved |
| Penalties | Late return, excess mileage, fuel charges |
| Taxes | Per tax type and jurisdiction |
| Total Amount | Sum of all line items |
| Payment Method | Credit card, debit, cash, corporate account |
| Currency | Transaction currency |
| Exchange Rate | If multi-currency, rate at time of transaction |
| Status | Draft → Finalized → Paid → Voided |

---

### 4.6 Security Deposits

| State | Description | Accounting Treatment |
|-------|-------------|---------------------|
| Pre-authorization / Hold | Card authorized but not charged | Disclosed on deposit statement; NOT recognized as revenue or liability |
| Captured | Funds deducted from customer | Recorded as a liability ("Customer Deposit" account) |
| Partially Released | Damage deducted; remainder returned | Damage portion moved to revenue/repair cost; remainder refunded |
| Fully Released | No damage; full refund | Liability reversed; no revenue impact |

**Requirements:**
- Pre-authorization holds must not appear as cash on the balance sheet.
- Capture events must create a liability journal entry with reference to the payment gateway authorization code.
- The system must track the hold expiry date and alert accounting if a hold expires before settlement.
- Deposit statements must be issued to the customer at hold, capture, and release events.

---

### 4.7 Refunds, Adjustments, and Disputes

**Workflow:**

```
Customer / Agent Request
        │
        ▼
Accounts Receivable Clerk — reviews request, attaches evidence
        │
        ▼
Accounting Manager — approves or rejects (amounts above threshold: $[configurable])
        │
        ▼
Finance Controller — second approval for amounts above $[configurable] or disputed charges
        │
        ▼
System applies adjustment → creates Credit Note or Refund transaction
        │
        ▼
Notification sent to customer + audit record written
```

**Business Rules:**
- Refunds must reference the original invoice and payment transaction.
- Partial refunds must create a credit note for the refunded amount only.
- All adjustments require a mandatory reason code from a configurable list.
- Approved adjustments must be reflected in the ERP export for the same business day they are processed.
- Disputes must have a status lifecycle: `Open → Under Review → Resolved (Approved/Rejected)`.

---

### 4.8 Accounting and ERP Integration

The system must support integration with the following categories of accounting/ERP systems:

| Category | Minimum Requirements |
|----------|---------------------|
| General Ledger (GL) | Journal entry push via REST API or file-based import (CSV/XML) |
| Accounts Receivable (AR) | Customer invoice synchronization |
| Accounts Payable (AP) | Vendor/repair invoices |
| Payment Gateway | Transaction confirmation, refund initiation, reconciliation feed |
| Tax Engine (optional) | External tax engine integration (e.g., Avalara, Vertex) if native calculation is insufficient |

**Integration Requirements:**
- Idempotent API calls: duplicate messages must not create duplicate transactions.
- Each outbound record must carry a unique `external_transaction_id` that the ERP can use as an idempotency key.
- Connectivity failures must queue records for retry; no financial record must be silently dropped.
- ERP integration credentials must be stored in a secrets manager (not in application configuration files).

---

### 4.9 Export Formats and Frequencies

| Export Type | Format | Frequency | Delivery Method |
|------------|--------|-----------|----------------|
| Daily GL journal entries | CSV / IIF / XML | End of business day (EOD) | SFTP or API push |
| AR invoice feed | CSV / JSON | Real-time + EOD batch | API push |
| Bank reconciliation feed | BAI2 / MT940 | Daily | SFTP |
| Tax report | CSV / XML | Monthly / Quarterly | Manual download or SFTP |
| Audit export | JSON | On-demand | Secure download portal |

**Requirements:**
- Each export file must include a header with generation timestamp, record count, and checksum.
- Failed exports must generate an alert to the integration engineering team.
- All export files must be stored in the system for the retention period defined in §4.13.

---

### 4.10 Chart of Accounts and Mapping

- The system must maintain a configurable Chart of Accounts (CoA) that maps each transaction component to a specific GL account code.
- Default CoA must cover at minimum:

| Account | Type |
|---------|------|
| Rental Revenue | Revenue |
| Add-on Revenue | Revenue |
| Deferred Revenue | Liability |
| Customer Deposit | Liability |
| Accounts Receivable | Asset |
| Sales Tax Payable | Liability |
| Damage Recovery | Revenue |
| Discounts Given | Contra-Revenue |
| Refunds Payable | Liability |
| Bank / Cash | Asset |

- The CoA must be configurable by the Finance Controller without engineering involvement.
- Mapping rules must support overrides per location, corporate account, or rental type.
- Changes to CoA mapping must be version-controlled and take effect from a configurable future date.

---

### 4.11 Multi-Currency Support

| Requirement | Details |
|------------|---------|
| Transaction currency | Must match the currency agreed at booking time |
| Settlement currency | Configurable per location/company entity |
| Exchange rate source | Configurable: manual entry, or auto-fetch from a rate provider (e.g., ECB, OpenExchangeRates) |
| Rate locking | Exchange rate must be locked at the time of invoice finalization |
| Revaluation | Period-end FX revaluation routine must calculate unrealized gains/losses on open AR balances |
| Reporting currency | All reports must support display in a single functional currency with FX conversion |

**Constraints:**
- Exchange rates must be stored with the original transaction and must not change retroactively.
- The system must support at minimum: USD, EUR, GBP, CAD, AUD, JPY.

---

### 4.12 Audit Trail

Every financial event must produce an immutable audit log entry containing:

| Field | Description |
|-------|-------------|
| Event ID | UUID |
| Timestamp | UTC datetime with millisecond precision |
| Event Type | e.g., `INVOICE_CREATED`, `PAYMENT_CAPTURED`, `REFUND_APPROVED` |
| Actor | User ID and role |
| IP Address | Source IP of the request |
| Resource Type | e.g., `Invoice`, `Transaction`, `CreditNote` |
| Resource ID | ID of the affected record |
| Before State | Snapshot of record prior to change (JSON) |
| After State | Snapshot of record after change (JSON) |
| Reason | Required for state changes affecting financial amounts |

**Requirements:**
- Audit records must be write-once; no update or delete operations are permitted on audit entries.
- Audit logs must be stored separately from transactional data and backed up independently.
- Audit logs must be queryable by actor, resource, date range, and event type.
- The system must alert the Accounting Manager for any direct database modification that bypasses the application layer (integrity monitoring).

---

### 4.13 Record Retention

| Record Type | Minimum Retention Period |
|-------------|--------------------------|
| Invoices and receipts | 7 years |
| Tax records | 7 years (or as required by jurisdiction) |
| Audit logs | 7 years |
| Security deposit records | 5 years after settlement |
| Dispute and adjustment records | 7 years |
| ERP export files | 5 years |
| Payment gateway transaction logs | 5 years |

**Requirements:**
- Records past their active period must be archived to low-cost storage but remain searchable and retrievable within 48 hours.
- Deletion of financial records must require multi-person authorization and generate an audit entry.
- The system must provide automated notifications 90 days before the end of the retention period for records approaching expiry.

---

### 4.14 Reconciliation Processes

The following reconciliation workflows must be supported:

| Reconciliation Type | Frequency | Process |
|--------------------|-----------|---------|
| Payment reconciliation | Daily | System transactions matched against payment gateway settlement report |
| Deposit reconciliation | Daily | Open deposit holds matched against bank/gateway hold reports |
| AR reconciliation | Monthly | Outstanding invoices matched against customer payments and credit notes |
| Refund reconciliation | Daily | Approved refunds matched against payment gateway refund confirmations |
| Tax reconciliation | Monthly/Quarterly | Calculated tax collected matched against tax authority filings |

**Requirements:**
- Unmatched items must be flagged in a reconciliation exception report.
- The AR clerk must be able to manually match, mark as exception, or escalate unmatched items.
- Reconciliation status must be retained as part of the financial record.

---

### 4.15 Reporting and Financial KPIs

The system must support the following reports accessible to the Accounting Manager and Finance Controller:

| Report | Key Metrics | Frequency |
|--------|------------|-----------|
| Revenue Summary | Total revenue by location, vehicle class, period | Daily / Monthly |
| Aging Receivables | Outstanding balances by age bucket (0–30, 31–60, 61–90, 90+ days) | Weekly |
| Occupancy / Utilization | Fleet revenue per available vehicle day | Monthly |
| Tax Collected | Tax amount by type and jurisdiction | Monthly / Quarterly |
| Deposit Liability | Total open deposits by status | Daily |
| Refunds and Adjustments | Refund volume, reason breakdown, approval path | Monthly |
| Cash Flow Forecast | Expected collections based on open AR | Weekly |
| ERP Sync Status | Records successfully exported vs. failed | Daily |

**Requirements:**
- All reports must be exportable to CSV and PDF.
- Reports must support filtering by date range, location, customer type, and currency.
- Scheduled report delivery via email must be configurable.

---

### 4.16 Access Controls and Authorization

| Role | Permissions |
|------|------------|
| Accounting Clerk | View invoices, record manual payments, initiate refund requests |
| Accounting Manager | All clerk permissions + approve refunds/adjustments up to configured threshold, manage CoA |
| Finance Controller | All manager permissions + approve large adjustments, run period-end routines, FX revaluation |
| Auditor (read-only) | Read access to all financial records and audit logs; no write access |
| System Administrator | Manage integrations, credentials, batch job scheduling; no access to financial record content |

**Requirements:**
- All financial write operations must verify the actor's role and record it in the audit trail.
- Approval workflows must enforce the configured monetary thresholds.
- Privileged access review must be performed quarterly.
- Segregation of duties: the same user must not be able to both create and approve an adjustment.

---

### 4.17 Damage and Repair Charges

**Workflow:**

```
Rental Return
     │
     ▼
Vehicle Inspection (Operations)
     │
     ▼
Damage Assessment — description, photo evidence, repair estimate
     │
     ▼
Accounting Notified — creates damage charge line item
     │
     ▼
Customer Notified — damage invoice issued
     │
     ▼
Charge deducted from deposit (if held) OR new invoice raised
     │
     ▼
Excess over deposit → new AR invoice for remaining amount
     │
     ▼
Repair cost posted to damage/repair expense account in GL
```

**Requirements:**
- Damage charges must be linked to the original rental and the inspection record.
- Damage invoices must itemize each damage item with description, estimated repair cost, and supporting evidence reference.
- If the damage charge is disputed by the customer, the dispute workflow in §4.7 applies.
- Repair costs incurred by the business must be tracked separately from customer-recovered amounts for P&L purposes.

---

### 4.18 SLAs for Financial Data

| Process | Target SLA |
|---------|-----------|
| Invoice generation after rental close | ≤ 1 hour |
| Payment recording after gateway confirmation | ≤ 5 minutes |
| EOD GL export completion | ≤ 2 hours after business day close |
| Audit log write latency | ≤ 1 second |
| Report generation (standard) | ≤ 30 seconds |
| Report generation (large date range, all locations) | ≤ 5 minutes |
| Record retrieval from archive | ≤ 48 hours |
| ERP export retry on failure | ≤ 15 minutes after failure detection |
| Financial data availability (uptime) | ≥ 99.9% |

---

### 4.19 Regulatory and Compliance Constraints

- **Tax filing:** The system must produce tax summary data compatible with VAT/GST returns and US sales-tax filings per jurisdiction.
- **Financial record keeping:** Compliance with local generally accepted accounting principles (GAAP) or IFRS as applicable.
- **Data privacy:** Financial records containing personal data must comply with GDPR / CCPA — customer PII must not be stored in raw audit log snapshots beyond the privacy retention period (subject to legal hold overrides).
- **PCI-DSS:** Card data must never be stored in the application; payment tokenization is mandatory. Only masked card details (last 4 digits, card type) may appear in invoice records.
- **SOX (if applicable):** If the organization is publicly traded, internal controls over financial reporting must be documented and auditable.
- **Insurance regulations:** Damage/repair invoicing must comply with applicable consumer protection laws for the jurisdiction of the rental.

---

## 5. MVP Acceptance Criteria

The following capabilities constitute the minimum viable product for the Accounting module:

| # | Feature | Acceptance Criteria |
|---|---------|---------------------|
| MVP-1 | Per-rental invoice generation | Every completed rental produces a finalized, numbered invoice with full line-item breakdown |
| MVP-2 | Payment recording | Cash, card, and corporate-account payments are recorded and linked to invoices |
| MVP-3 | Security deposit lifecycle | Hold, capture, and release events are recorded with correct accounting treatment |
| MVP-4 | Basic tax calculation | Tax is calculated and shown as a separate line item for at least one jurisdiction |
| MVP-5 | Audit trail | All invoice create, update, and payment events are logged with actor and timestamp |
| MVP-6 | ERP export (CSV) | A daily CSV export of GL journal entries can be generated and downloaded |
| MVP-7 | Basic reports | Revenue summary and aging receivables report are available and exportable |
| MVP-8 | Role-based access | At least Clerk, Manager, and read-only Auditor roles are enforced |
| MVP-9 | Refund workflow | A refund request can be submitted, approved, and reflected in the invoice and GL |
| MVP-10 | Deposit statement | A deposit statement is issued to the customer at deposit capture and release |

---

## 6. Non-Functional Requirements

| Category | Requirement |
|----------|------------|
| **Performance** | EOD batch processes complete within 2 hours; standard reports render in ≤ 30 seconds |
| **Scalability** | System must handle up to 10,000 rental transactions per day without degradation |
| **Availability** | Financial module availability ≥ 99.9% (excluding planned maintenance) |
| **Data Integrity** | All financial transactions must be ACID-compliant; no partial writes |
| **Security** | Encryption at rest (AES-256) and in transit (TLS 1.2+) for all financial data |
| **Backup** | Financial data backed up daily; point-in-time recovery to within 1 hour of any failure |
| **Disaster Recovery** | RTO ≤ 4 hours; RPO ≤ 1 hour for financial data |
| **Idempotency** | Duplicate payment or export messages must not create duplicate GL entries |

---

## 7. Engineering and Operations Considerations

### 7.1 Data Integrity and Idempotency

- All financial write operations must be wrapped in database transactions; partial commits must roll back fully.
- Payment events received from gateways must carry a unique event ID; the system must deduplicate on this ID before creating records.
- Journal entry creation must be idempotent: replaying the same batch export must not post duplicate entries to the ERP.

### 7.2 Secure Storage and Access Controls

- Financial tables must be separated from operational data at the database level (schema isolation or separate database).
- Column-level encryption must be applied to sensitive fields (e.g., total amounts, customer tax IDs) where required by policy.
- Database credentials for the financial schema must be rotated on a scheduled basis and stored in a secrets manager.
- All queries against financial tables must be logged at the application layer.

### 7.3 Backup, Retention, and Disaster Recovery

- Financial data must be included in daily full backups and continuous transaction-log backups.
- Backups must be stored in a geographically separate region.
- Restoration procedures must be tested quarterly; restoration time must be documented and must meet the RPO/RTO SLAs.
- Archive storage (for records past active retention) must remain queryable; a retrieval SLA of 48 hours must be met.

### 7.4 Performance for Batch Jobs and Reconciliation

- EOD batch jobs (GL export, reconciliation matching) must be scheduled off-peak (e.g., after business hours) and must not degrade real-time transaction performance.
- Batch jobs must be re-entrant: if a job is interrupted and restarted, it must continue from the last committed checkpoint without duplicating work.
- Database indexes on `rental_id`, `customer_id`, `invoice_date`, and `status` must be maintained to support reconciliation queries at scale.
- Reconciliation jobs must generate a completion report showing records processed, matched, unmatched, and any errors.

---

## 8. Out of Scope

The following items are explicitly out of scope for the initial Accounting module:

- Payroll processing and employee expense management.
- Vendor/supplier invoice management (AP module — future phase).
- Real-time streaming analytics dashboards (batch reporting sufficient for MVP).
- Direct integration with insurance claim systems (manual process at MVP).
- Cryptocurrency payment support.

---

## 9. Glossary

| Term | Definition |
|------|-----------|
| **CoA** | Chart of Accounts — a structured list of GL account codes |
| **Credit Note** | Document issued to reduce the amount owed by a customer |
| **Deferred Revenue** | Revenue received but not yet earned; recorded as a liability |
| **EOD** | End of Day — close of the business day for accounting purposes |
| **ERP** | Enterprise Resource Planning system (e.g., SAP, Oracle, NetSuite, QuickBooks) |
| **FX** | Foreign Exchange |
| **GL** | General Ledger |
| **Idempotency** | Property ensuring that performing an operation multiple times produces the same result as performing it once |
| **IIF** | Intuit Interchange Format — QuickBooks-compatible import file |
| **PCI-DSS** | Payment Card Industry Data Security Standard |
| **Pro-rated** | Charge calculated proportionally for a partial period |
| **RPO** | Recovery Point Objective — maximum acceptable data loss measured in time |
| **RTO** | Recovery Time Objective — maximum acceptable downtime before recovery |
| **SOX** | Sarbanes-Oxley Act — US federal law governing financial reporting controls |
| **VAT** | Value Added Tax |
