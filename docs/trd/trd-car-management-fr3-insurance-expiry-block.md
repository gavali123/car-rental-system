# TRD - Car Management: FR-3 Insurance Expiry Block

## Document Information

| Field | Details |
|---|---|
| **Feature Name** | Insurance Expiry Block |
| **Module** | Car Management |
| **PRD Reference** | [FR-3: Insurance Expiry Block](../prd/prd-car-management.md#fr-3-insurance-expiry-block) |
| **Author** | @copilot |
| **Date** | 2026-03-07 |
| **Version** | 1.0 |

---

## Table of Contents

1. [Overview](#overview)
2. [Scope](#scope)
3. [Data Model Design](#data-model-design)
   - [Vehicle Insurance Record](#vehicle-insurance-record)
   - [Insurance Expiry Block Log](#insurance-expiry-block-log)
4. [Business Logic](#business-logic)
   - [Expiry Threshold Definition](#expiry-threshold-definition)
   - [Daily Expiry Check Algorithm](#daily-expiry-check-algorithm)
   - [Block Condition Evaluation](#block-condition-evaluation)
   - [Block Lift Condition Evaluation](#block-lift-condition-evaluation)
5. [Process Flows](#process-flows)
   - [PF-1: Daily Expiry Check and Block Trigger](#pf-1-daily-expiry-check-and-block-trigger)
   - [PF-2: Insurance Record Update and Block Lift](#pf-2-insurance-record-update-and-block-lift)
   - [PF-3: Vehicle Record View with Expired Insurance](#pf-3-vehicle-record-view-with-expired-insurance)
   - [PF-4: Reservation Allocation Guard](#pf-4-reservation-allocation-guard)
6. [Notification Design](#notification-design)
   - [Alert Types](#alert-types)
   - [Email Alert](#email-alert)
   - [SMS Alert](#sms-alert)
   - [Dashboard Alert](#dashboard-alert)
7. [API Contract](#api-contract)
8. [Non-Functional Requirements](#non-functional-requirements)
9. [Acceptance Criteria — Technical Mapping](#acceptance-criteria--technical-mapping)
10. [Dependencies](#dependencies)
11. [Risks and Open Questions](#risks-and-open-questions)

---

## Overview

This document defines the technical design for the **Insurance Expiry Block** feature (FR-3) of the Car Management module. The feature ensures that any active vehicle whose insurance is about to expire (within 7 calendar days) or has already expired is automatically blocked from reservation allocation, and that fleet managers are proactively alerted so they can act before liability exposure occurs.

---

## Scope

**In Scope:**

- Scheduled daily check of insurance expiry dates for all active vehicles.
- Automatic availability block applied to vehicles with insurance expiring within 7 calendar days or already expired.
- Automatic block lift when a fleet manager records a new valid insurance expiry date beyond the 7-day threshold.
- Tri-channel alert to fleet managers: email, SMS, and in-application dashboard notification.
- Prominent expired/expiring insurance warning displayed on the vehicle detail record.
- Guard at the reservation allocation layer to reject assignment of blocked vehicles.

**Out of Scope:**

- Integration with insurance providers or third-party policy management systems.
- Automated renewal or procurement of insurance policies.
- Per-country or jurisdiction-specific insurance compliance rules.
- Mobile-native push notifications (desktop web only; mobile web supported for field crew forms only).

---

## Data Model Design

### Vehicle Insurance Record

The insurance details for each vehicle are stored as part of the vehicle's master record. The following fields are required to support this feature:

| Field | Type | Description |
|---|---|---|
| `vehicle_id` | UUID (FK) | Reference to the vehicle record. |
| `insurer_name` | Text | Name of the insurance provider. |
| `policy_number` | Text | Unique policy identifier issued by the insurer. |
| `coverage_start_date` | Date | Date from which the insurance policy is effective. |
| `coverage_end_date` | Date | Date on which the insurance policy expires. This is the field evaluated by the expiry block logic. |
| `insurance_blocked` | Boolean | Flag indicating whether the vehicle has been blocked due to insurance expiry. Defaults to `false`. |
| `block_triggered_at` | Timestamp (nullable) | The date and time at which the insurance block was most recently activated. |
| `updated_at` | Timestamp | Timestamp of the last modification to the insurance record. |
| `updated_by` | UUID (FK) | Reference to the user who last updated the insurance record. |

> The `insurance_blocked` flag is the authoritative source for whether a vehicle is excluded from reservation allocation due to insurance reasons. The block evaluation logic writes to this flag during the daily check and clears it upon a valid insurance update.

### Insurance Expiry Block Log

Every activation and deactivation of the insurance block must be recorded in an audit log for traceability and compliance purposes.

| Field | Type | Description |
|---|---|---|
| `log_id` | UUID | Unique identifier for each log entry. |
| `vehicle_id` | UUID (FK) | Reference to the vehicle record. |
| `event_type` | Enum | `BLOCK_APPLIED` or `BLOCK_LIFTED`. |
| `coverage_end_date_at_event` | Date | The `coverage_end_date` value at the time of the event. |
| `days_to_expiry` | Integer | Number of days between the current date and `coverage_end_date` at the time of the event. Negative values indicate days since expiry. |
| `triggered_by` | Enum | `SCHEDULED_CHECK` (system-triggered) or `MANUAL_UPDATE` (fleet manager update). |
| `actor_id` | UUID (FK, nullable) | User who performed the action; `null` for `SCHEDULED_CHECK` events. |
| `occurred_at` | Timestamp | Date and time the event was recorded. |

---

## Business Logic

### Expiry Threshold Definition

The insurance expiry threshold is defined as:

> A vehicle's insurance is considered **near-expiry** when:
> `coverage_end_date` − `current_date` ≤ 7 calendar days

> A vehicle's insurance is considered **expired** when:
> `coverage_end_date` < `current_date`

Both near-expiry and expired conditions trigger or maintain the `insurance_blocked = true` state. Calendar days are used (not business days).

### Daily Expiry Check Algorithm

The system runs a scheduled job once per day. The recommended run time is **00:05 local system time** (shortly after midnight) so that newly expired or near-expiry insurance records are caught at the earliest opportunity each day.

The algorithm, described in steps:

1. Query all vehicles where `lifecycle_status = 'Active'`.
2. For each active vehicle, compute `days_remaining = coverage_end_date − current_date`.
3. **If `days_remaining ≤ 7`:**
   - If `insurance_blocked` is currently `false`:
     - Set `insurance_blocked = true`.
     - Record `block_triggered_at = now()`.
     - Write a `BLOCK_APPLIED` entry to the Insurance Expiry Block Log.
     - Dispatch alert notifications to the fleet manager (email, SMS, dashboard).
   - If `insurance_blocked` is already `true`:
     - No state change. No duplicate notifications. No log entry.
4. **If `days_remaining > 7`:**
   - If `insurance_blocked` is currently `true` (indicating it was set by a previous run and not yet cleared):
     - Set `insurance_blocked = false`.
     - Clear `block_triggered_at`.
     - Write a `BLOCK_LIFTED` entry to the Insurance Expiry Block Log.
     - *(This scenario handles the edge case where a policy update is recorded without going through the normal fleet manager update flow, e.g., a data correction.)*
   - If `insurance_blocked` is already `false`:
     - No action.

> **Idempotency:** The daily check is idempotent. Running it multiple times on the same day produces the same outcome. Re-running does not produce duplicate log entries or duplicate notifications.

### Block Condition Evaluation

The `insurance_blocked` flag is evaluated at two points:

1. **During the daily scheduled check** — as described above.
2. **At reservation allocation time** — the allocation engine reads `insurance_blocked` before assigning a vehicle to a reservation. If `insurance_blocked = true`, the vehicle is excluded regardless of its `lifecycle_status`. See [PF-4: Reservation Allocation Guard](#pf-4-reservation-allocation-guard).

### Block Lift Condition Evaluation

The block is lifted when a fleet manager updates the vehicle's insurance record with a new `coverage_end_date` such that `coverage_end_date − current_date > 7`. The lift is applied immediately upon saving the insurance record update — it does not wait for the next daily check. Steps:

1. Fleet manager submits an insurance record update with a new `coverage_end_date`.
2. The system validates that `new_coverage_end_date > current_date + 7 days`.
3. If validation passes:
   - Persist the updated insurance record fields.
   - Set `insurance_blocked = false`.
   - Clear `block_triggered_at`.
   - Write a `BLOCK_LIFTED` entry to the Insurance Expiry Block Log with `triggered_by = 'MANUAL_UPDATE'`.
4. If validation fails (new date is still within or before the 7-day window):
   - Persist the updated insurance record fields.
   - Keep `insurance_blocked = true` (no change to block state).
   - Display a warning to the fleet manager indicating that the vehicle remains blocked because the new expiry date is still within the 7-day threshold.

---

## Process Flows

### PF-1: Daily Expiry Check and Block Trigger

```
Scheduled Job (00:05 daily)
        │
        ▼
 Query all Active vehicles
        │
        ▼
 For each vehicle:
 Compute days_remaining = coverage_end_date − today
        │
        ├─── days_remaining > 7 ──────────────────────────────────► No action
        │
        └─── days_remaining ≤ 7
                    │
                    ├─── insurance_blocked already = true ─────────► No action (idempotent)
                    │
                    └─── insurance_blocked = false
                                │
                                ▼
                       Set insurance_blocked = true
                       Set block_triggered_at = now()
                       Write BLOCK_APPLIED log entry
                                │
                                ▼
                       Dispatch Alerts:
                       - Email to fleet manager(s)
                       - SMS to fleet manager(s)
                       - Dashboard notification
```

### PF-2: Insurance Record Update and Block Lift

```
Fleet Manager submits insurance update
(new coverage_end_date provided)
        │
        ▼
 Validate: new_coverage_end_date > today + 7 days?
        │
        ├─── NO (still within or past threshold)
        │           │
        │           ▼
        │     Persist new insurance record
        │     Keep insurance_blocked = true
        │     Display warning: "Vehicle remains blocked —
        │     new expiry date is still within 7-day threshold"
        │
        └─── YES (new date is beyond threshold)
                    │
                    ▼
             Persist new insurance record
             Set insurance_blocked = false
             Clear block_triggered_at
             Write BLOCK_LIFTED log entry (MANUAL_UPDATE)
                    │
                    ▼
             Vehicle is immediately available
             for reservation allocation again
```

### PF-3: Vehicle Record View with Expired Insurance

```
User opens vehicle detail record
        │
        ▼
 System reads coverage_end_date and insurance_blocked
        │
        ├─── coverage_end_date < today (expired)
        │           │
        │           ▼
        │     Display prominent banner:
        │     "⚠ Insurance Expired — This vehicle is
        │      blocked from reservation allocation.
        │      Expiry date: {coverage_end_date}"
        │
        ├─── coverage_end_date − today ≤ 7 (near-expiry)
        │           │
        │           ▼
        │     Display prominent warning:
        │     "⚠ Insurance Expiring Soon — {N} days remaining.
        │      This vehicle is blocked from reservation allocation."
        │
        └─── coverage_end_date − today > 7 (valid)
                    │
                    ▼
             Display insurance details normally
             No warning banner shown
```

### PF-4: Reservation Allocation Guard

```
Reservation allocation system selects candidate vehicles
        │
        ▼
 For each candidate vehicle:
 Check lifecycle_status = 'Active'  AND  insurance_blocked = false
        │
        ├─── FAILS either check ────────────────────────────────► Exclude from allocation
        │
        └─── PASSES both checks ────────────────────────────────► Include in allocation pool
```

---

## Notification Design

### Alert Types

Three alert channels are used. All three are dispatched simultaneously when the daily check activates a new block.

| Channel | Trigger | Recipient |
|---|---|---|
| Email | New `BLOCK_APPLIED` event | All fleet managers in the system |
| SMS | New `BLOCK_APPLIED` event | All fleet managers with a registered mobile number |
| Dashboard Notification | New `BLOCK_APPLIED` event | All fleet managers currently logged in; persisted for those not currently logged in |

> Alerts are sent **once per block activation**, not once per daily check run. Re-running the daily check on an already-blocked vehicle does not generate repeat notifications.

### Email Alert

**Subject:**  
`[Action Required] Vehicle Insurance Expiring — {Vehicle Plate} | {N} days remaining`

**Body Content:**

| Section | Content |
|---|---|
| Greeting | Addressed to the fleet manager by name. |
| Vehicle summary | License plate, make, model, VIN. |
| Insurance details | Policy number, insurer name, `coverage_end_date`. |
| Days remaining | Number of calendar days until expiry (negative value if already expired). |
| Block status | Confirmation that the vehicle has been automatically blocked from reservation allocation. |
| Required action | Instruction to update the insurance record with a valid new expiry date to lift the block. |
| Link | Direct deep link to the vehicle's insurance record in the application. |
| Footer | System-generated notice; do not reply. |

### SMS Alert

**Message format:**  
`[Car Rental] URGENT: Vehicle {Plate} insurance expires {coverage_end_date} ({N} days). Blocked from allocation. Update insurance immediately.`

Maximum 160 characters. If the message exceeds 160 characters due to plate length, the text is truncated at the vehicle plate and expiry date while preserving the action instruction.

### Dashboard Alert

A persistent in-application notification is created with the following properties:

| Property | Value |
|---|---|
| **Severity** | `WARNING` if days_remaining ∈ [1, 7]; `CRITICAL` if days_remaining ≤ 0 |
| **Icon** | Warning triangle |
| **Title** | "Insurance Expiring Soon" (WARNING) or "Insurance Expired" (CRITICAL) |
| **Body** | "{Vehicle plate} — {N} days remaining. Vehicle is blocked from allocation." |
| **Link** | Deep link to vehicle insurance record |
| **Dismissible** | No (cannot be dismissed while block is active) |
| **Auto-resolve** | Automatically removed from the notification centre when the block is lifted |

Dashboard notifications persist across sessions. A fleet manager who was offline when the daily check ran will see the notification the next time they log in.

---

## API Contract

The following API endpoints are required to support this feature. Exact implementation detail (e.g., HTTP framework, authentication mechanism) is determined by the engineering team.

### Retrieve Vehicle Insurance Details

**Purpose:** Returns the insurance record and current block status for a specific vehicle.

| Attribute | Value |
|---|---|
| Method | `GET` |
| Path | `/api/vehicles/{vehicle_id}/insurance` |
| Auth | Required — fleet manager or admin role |
| Response Fields | `vehicle_id`, `insurer_name`, `policy_number`, `coverage_start_date`, `coverage_end_date`, `insurance_blocked`, `block_triggered_at`, `days_remaining` (computed) |
| Error Cases | `404` if vehicle not found; `403` if caller lacks permission |

### Update Vehicle Insurance Record

**Purpose:** Allows a fleet manager to update the insurance record. If the new `coverage_end_date` is beyond the 7-day threshold, the block is automatically lifted.

| Attribute | Value |
|---|---|
| Method | `PATCH` |
| Path | `/api/vehicles/{vehicle_id}/insurance` |
| Auth | Required — fleet manager or admin role |
| Request Fields | `insurer_name` (optional), `policy_number` (optional), `coverage_start_date` (optional), `coverage_end_date` (required) |
| Response Fields | Updated insurance record including new `insurance_blocked` status |
| Side Effects | Block lifted if new `coverage_end_date > today + 7`; audit log entry written |
| Error Cases | `400` if `coverage_end_date` is missing or invalid format; `404` if vehicle not found |

### Retrieve Insurance Block Log

**Purpose:** Returns the full history of block activations and deactivations for a vehicle. Used for audit and compliance review.

| Attribute | Value |
|---|---|
| Method | `GET` |
| Path | `/api/vehicles/{vehicle_id}/insurance/block-log` |
| Auth | Required — fleet manager or admin role |
| Response Fields | Paginated list of log entries: `log_id`, `event_type`, `coverage_end_date_at_event`, `days_to_expiry`, `triggered_by`, `actor_id`, `occurred_at` |
| Error Cases | `404` if vehicle not found |

### Trigger Manual Expiry Check (Admin / Ops Tooling)

**Purpose:** Allows an administrator to manually trigger the insurance expiry check outside of the scheduled run. Intended for operational troubleshooting and testing.

| Attribute | Value |
|---|---|
| Method | `POST` |
| Path | `/api/admin/insurance-expiry-check` |
| Auth | Required — admin role only |
| Request Fields | `vehicle_id` (optional): if provided, checks only this vehicle; if omitted, checks all active vehicles |
| Response Fields | Summary: number of vehicles checked, number of blocks applied |
| Side Effects | Same as scheduled check — idempotent |

---

## Non-Functional Requirements

| ID | Category | Requirement |
|---|---|---|
| NFR-1 | Performance | The daily expiry check must complete processing for the entire active fleet within 5 minutes, regardless of fleet size. |
| NFR-2 | Reliability | The daily scheduled job must be monitored. If it fails to run, an operational alert must be raised within 1 hour. A missed run must be recoverable by manual trigger without data loss. |
| NFR-3 | Accuracy | The check must use the application's canonical current date (UTC), not the server's local time zone, to avoid edge cases at day boundaries across locations. |
| NFR-4 | Auditability | Every block activation and deactivation must be recorded in the Insurance Expiry Block Log with a full immutable audit trail. Log entries must not be editable or deletable by any user role. |
| NFR-5 | Consistency | The `insurance_blocked` flag must be kept consistent with the block log. Any update that changes the flag must atomically write the corresponding log entry in the same transaction. |
| NFR-6 | Notifications | Alert delivery (email and SMS) must be attempted within 5 minutes of block activation. Dashboard notifications must appear immediately. |
| NFR-7 | Security | Only users with the **fleet manager** or **admin** role may view or update insurance records. Unauthorized access attempts must return `403 Forbidden` and be logged. |
| NFR-8 | Availability | The insurance block guard in the reservation allocation service must not introduce more than 10 ms of additional latency per allocation request. |

---

## Acceptance Criteria — Technical Mapping

The following table maps each PRD acceptance criterion to the technical components responsible for satisfying it.

| AC # | PRD Acceptance Criterion | Technical Fulfilment |
|---|---|---|
| AC-1 | Given a vehicle's insurance expiry date is 7 or fewer days away, when the system performs its daily check, then the vehicle is flagged as unavailable for new reservations and an alert is sent via email, SMS, and dashboard. | Daily scheduled job (PF-1) sets `insurance_blocked = true`, writes a `BLOCK_APPLIED` log entry, and dispatches tri-channel alerts. |
| AC-2 | Given a vehicle has been flagged due to expiring insurance, when the fleet manager updates the insurance record with a new valid expiry date beyond 7 days, then the block is automatically lifted and the vehicle becomes available again. | Insurance record `PATCH` endpoint (PF-2) evaluates the new date, sets `insurance_blocked = false`, writes a `BLOCK_LIFTED` log entry with `triggered_by = MANUAL_UPDATE`. |
| AC-3 | Given a vehicle's insurance has already expired, when a user views its record, then the system prominently displays the expired insurance warning. | Vehicle detail view logic (PF-3) checks `coverage_end_date < today` and renders a `CRITICAL` severity banner. |

Additionally, the reservation allocation guard (PF-4) ensures that no vehicle with `insurance_blocked = true` can be assigned, which is an implicit requirement from the PRD statement.

---

## Dependencies

| Dependency | Type | Description |
|---|---|---|
| Vehicle Master Record | Internal | The insurance fields (`coverage_end_date`, `insurance_blocked`, `block_triggered_at`) must be part of the vehicle master data model. This feature cannot be built until the vehicle record schema is finalised (FR-1: Vehicle Onboarding). |
| Reservation Allocation Engine | Internal | FR-11 (Automatic Reservation Allocation) must integrate the `insurance_blocked` guard. The allocation service must read this flag from the vehicle record before including a vehicle in the candidate pool. |
| Notification Service | Internal | A shared notification service capable of dispatching email, SMS, and in-application dashboard notifications must be available. This feature depends on that service being implemented and reachable. |
| Scheduled Job Infrastructure | Infrastructure | A scheduler (e.g., cron, job queue) must be available to trigger the daily expiry check. The infrastructure team must provision and monitor this scheduler. |
| Fleet Manager User Role | Internal | The system must have a defined **fleet manager** role with an associated contact record (email address, mobile number) to receive alerts. This depends on the user management module being available. |

---

## Risks and Open Questions

| ID | Type | Description | Recommended Resolution |
|---|---|---|---|
| RQ-1 | Open Question | Should the 7-day threshold be configurable per deployment, or is it a fixed system constant? | Confirm with product: if the business may want to change the threshold in the future (e.g., 14 days for luxury fleet), design as a system configuration value rather than a hardcoded constant. |
| RQ-2 | Open Question | Should notifications be sent only once per block activation, or also as a daily reminder while the vehicle remains blocked? | Confirm with product and fleet operations team. Daily reminders provide more visibility but risk alert fatigue. |
| RQ-3 | Open Question | How is the "fleet manager" recipient list determined? Is it all users with the fleet manager role, or is there a notification subscription model? | Define in the user management or notification service design. Default behaviour: all active users with the fleet manager role. |
| RQ-4 | Risk | If the daily check job fails silently (no error, no run), vehicles may not be blocked in time. | Implement job execution monitoring with an operational alert if the job has not run within 25 hours. |
| RQ-5 | Risk | Clock skew between application servers and the database could cause the `days_remaining` calculation to differ slightly. | Enforce that all date calculations use the database server's `CURRENT_DATE` (UTC) as the authoritative source, not the application server's system time. |
| RQ-6 | Open Question | What happens when a vehicle transitions from **Active** to **Maintenance** status while it is also insurance-blocked? The `insurance_blocked` flag should remain set and be re-evaluated when the vehicle is returned to **Active** status. | Confirm expected behaviour: the insurance block should persist across status changes and be re-evaluated only when the vehicle is **Active**. |
