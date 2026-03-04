# Product Requirement Document — Marketing Module
## Car Rental System

| Field | Value |
|---|---|
| **Document version** | 1.0 |
| **Status** | Draft |
| **Owner** | Product Owner — Planning & Requirement Analysis |
| **Last updated** | 2026-03-04 |

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Business Context & Objectives](#2-business-context--objectives)
3. [Target Customer Segments](#3-target-customer-segments)
4. [Promotions & Campaigns](#4-promotions--campaigns)
5. [Loyalty Program](#5-loyalty-program)
6. [Customer Segmentation Attributes](#6-customer-segmentation-attributes)
7. [CRM & Platform Integrations](#7-crm--platform-integrations)
8. [Marketing Analytics & KPIs](#8-marketing-analytics--kpis)
9. [Data Freshness & Real-Time Requirements](#9-data-freshness--real-time-requirements)
10. [Customer Consent & Data Privacy](#10-customer-consent--data-privacy)
11. [Marketing Channels](#11-marketing-channels)
12. [A/B Testing](#12-ab-testing)
13. [Campaign Workflow — Creation, Approval & Scheduling](#13-campaign-workflow--creation-approval--scheduling)
14. [Personalization & Predictive Offers](#14-personalization--predictive-offers)
15. [Customer Data Lifecycle & Retention Policies](#15-customer-data-lifecycle--retention-policies)
16. [Regulatory & Brand Guidelines](#16-regulatory--brand-guidelines)
17. [Reporting & Export Capabilities](#17-reporting--export-capabilities)
18. [Alerts & Automated Notifications](#18-alerts--automated-notifications)
19. [Promotional Abuse & Misuse Detection](#19-promotional-abuse--misuse-detection)
20. [MVP Acceptance Criteria](#20-mvp-acceptance-criteria)
21. [Engineering & Operational Considerations](#21-engineering--operational-considerations)
22. [Open Questions & Assumptions](#22-open-questions--assumptions)

---

## 1. Executive Summary

The company is expanding from car sales into car rental as a new line of business. This PRD defines the marketing capabilities required to support customer acquisition, engagement, and retention for the rental product. The scope covers promotional management, loyalty programs, campaign workflows, CRM integrations, analytics, compliance, and the operational constraints that engineering must satisfy.

---

## 2. Business Context & Objectives

**Interview question 1 — Primary marketing objectives**

| Objective | Description | Priority |
|---|---|---|
| **Awareness** | Build brand recognition for the rental offering among existing car-sales customers and new audiences. | High |
| **Customer acquisition** | Convert prospects (tourists, business travelers, corporate accounts) into first-time rental customers. | High |
| **Retention & loyalty** | Drive repeat rentals through loyalty incentives, personalised offers, and exceptional post-rental engagement. | High |
| **Revenue per customer** | Increase average rental value through upsells (insurance, GPS, premium vehicles) and cross-sells. | Medium |

---

## 3. Target Customer Segments

**Interview question 2 — Customer segments**

| Segment | Description | Initial Priority |
|---|---|---|
| **Business travelers** | Frequent short-duration rentals, expense-managed, value convenience and speed. | Tier 1 |
| **Tourists** | Seasonal, location-driven demand; respond to bundled deals with hotels/flights. | Tier 1 |
| **Local short-term renters** | Occasional renters for personal errands, day trips, or vehicle replacement. | Tier 2 |
| **Long-term corporate accounts** | Fleet-level agreements with invoicing, dedicated account management, and SLA requirements. | Tier 2 |

---

## 4. Promotions & Campaigns

### 4.1 Promotion Types

**Interview question 3 — Promotional types**

| Type | Description | Example |
|---|---|---|
| **Discount codes** | Fixed or percentage discount applied at checkout. | `SUMMER20` — 20% off |
| **Seasonal campaigns** | Time-bounded offers aligned with demand peaks (holidays, school breaks). | "Peak Season Flash Sale" |
| **Bundles** | Rental + add-on packages (e.g., GPS + child seat + full insurance). | "Family Road Trip Bundle" |
| **Loyalty points** | Points earned per rental day or spend, redeemable for discounts or free days. | 10 pts / £1 spent |
| **Referral bonuses** | Credit or discount issued to referrer and referee upon first completed referral rental. | £15 credit each |

### 4.2 Promotional Rules

**Interview question 4 — Promotional rule modeling**

| Rule Dimension | Requirement |
|---|---|
| **Stacking rules** | Configurable per promotion: exclusive (no stacking), additive (stack with others of same type), or capped stack (total discount ≤ X%). Default is exclusive for discount codes; loyalty redemption always additive up to 50% of rental value. |
| **Eligibility** | Rules may restrict by: customer segment, rental history (e.g., first rental only), vehicle category, rental duration, pickup location, or membership tier. |
| **Time windows** | Each promotion has a mandatory `start_date` / `end_date` (UTC). Optionally: `booking_window` (dates on which booking must occur) and `rental_window` (dates on which pickup must occur). |
| **Geofencing** | Promotions may be restricted to one or more pickup/drop-off locations or geographic regions (city, country). Location filters are evaluated against the customer's selected pickup branch. |

**Data model sketch — Promotion entity:**

```
Promotion {
  id, name, description, type (DISCOUNT_CODE | SEASONAL | BUNDLE | LOYALTY | REFERRAL),
  discount_type (PERCENT | FIXED), discount_value,
  stacking_policy (EXCLUSIVE | ADDITIVE | CAPPED),
  max_stack_discount_pct,
  eligibility_rules: [{ attribute, operator, value }],
  start_date, end_date, booking_window_start, booking_window_end,
  geo_filter: [location_id | region_code],
  usage_limit_total, usage_limit_per_customer,
  status (DRAFT | ACTIVE | PAUSED | EXPIRED)
}
```

---

## 5. Loyalty Program

**Interview question 5 — Tiered loyalty program**

### 5.1 Tier Structure

| Tier | Qualifying threshold (rolling 12 months) | Status benefits |
|---|---|---|
| **Bronze** | 0 pts (default) | 10 pts / £1 |
| **Silver** | ≥ 1,000 pts earned | 12 pts / £1; 5% discount on all rentals |
| **Gold** | ≥ 5,000 pts earned | 15 pts / £1; 10% discount; free category upgrade (subject to availability) |
| **Platinum** | ≥ 15,000 pts earned | 20 pts / £1; 15% discount; dedicated support line; guaranteed car class |

### 5.2 Earning Rules

- Points are earned on **completed** rentals only (not cancelled or disputed bookings).
- Base earning rate: 10 pts per £1 of net rental spend (excluding taxes, third-party fees).
- Bonus multiplier events: double-points weekends, new-vehicle-category first rental, referral.
- Points are credited within **24 hours** of rental completion.

### 5.3 Redemption Rules

- Minimum redemption: 500 pts = £5 discount.
- Points cannot be redeemed against taxes or mandatory insurance surcharges.
- Combined loyalty redemption plus other stackable promotions may not exceed 50% of net rental value.
- Unredeemed points expire after **24 months** of account inactivity.

### 5.4 Tier Review Cycle

- Tier status is reviewed on a **rolling 12-month** basis.
- Downgrade occurs if the qualifying threshold is not maintained; the customer is notified 30 days before a potential downgrade.

---

## 6. Customer Segmentation Attributes

**Interview question 6 — Customer attributes for segmentation**

| Attribute Category | Specific Attributes |
|---|---|
| **Demographic** | Age, country/city of residence, language preference, driving licence country |
| **Behavioural — rental** | Lifetime rental count, rentals in last 12 months, average rental duration, preferred vehicle category, pickup/drop-off location patterns |
| **Financial** | Total lifetime spend, average order value, outstanding credits/deposits |
| **Loyalty** | Current tier, points balance, last activity date, referral count |
| **Communication** | Opt-in status per channel (email, SMS, push, in-app), last engagement date, preferred contact time |
| **Corporate** | Corporate account flag, company name, cost-centre code, approved vehicle class |

---

## 7. CRM & Platform Integrations

**Interview question 7 — CRM and marketing platform integrations**

| Platform | Integration Type | Data Flow | Notes |
|---|---|---|---|
| **Salesforce CRM** | REST API (Salesforce Connect / Data Cloud) | Bidirectional: customer profile, rental history, loyalty status → Salesforce; Salesforce campaign membership → rental system | OAuth 2.0; near-real-time sync via event streaming |
| **HubSpot** | REST API (Contacts, Deals, Campaigns) | Outbound: new customers, booking events; Inbound: campaign membership updates | Webhook receiver in rental system |
| **Mailchimp / Klaviyo** | REST + Webhook | Outbound: audience segments, unsubscribe events; Inbound: campaign performance metrics | Frequency-cap enforcement in rental system |
| **Twilio (SMS)** | REST API | Outbound only: transactional and promotional SMS | STOP/UNSTOP consent flag must be honoured within 15 min |
| **Firebase Cloud Messaging** | REST API | Outbound: push notifications to mobile app | Requires device token management |
| **Google Analytics 4 / GTM** | Server-side events + GTM | Outbound: booking funnel events, promo application events | No PII in GA4 event parameters |
| **Meta Ads / Google Ads** | Conversions API / Enhanced Conversions | Outbound: hashed customer events for ad attribution | Consent check before sending |

All third-party integrations must support **retry with exponential back-off** and **dead-letter queuing** for failed deliveries.

---

## 8. Marketing Analytics & KPIs

**Interview question 8 — Analytics and KPIs**

| KPI | Definition | Target (MVP baseline) |
|---|---|---|
| **Conversion rate** | % of marketing-attributed sessions that result in a completed booking | ≥ 3% (to be validated) |
| **Promo ROI** | (Revenue from promo bookings − discount cost) / promo cost | > 0 at 90-day horizon |
| **Customer Lifetime Value (LTV)** | Predicted net revenue from a customer over 24 months | Tracked; threshold TBD post-launch |
| **Churn rate** | % of active customers with no rental in 12 months | < 20% annual |
| **Channel attribution** | First-touch, last-touch, and linear multi-touch attribution models | All three models available in reporting |
| **Promo redemption rate** | % of issued codes/promotions that are redeemed | Monitored per campaign |
| **Email open / click-through rates** | Standard email engagement metrics | Benchmarked against industry average |
| **Cost per acquisition (CPA)** | Total campaign spend / new customers acquired | Tracked per channel |

---

## 9. Data Freshness & Real-Time Requirements

**Interview question 9 — Real-time marketing data**

| Data Type | Freshness Requirement | Delivery Mechanism |
|---|---|---|
| **Booking events** (new, modified, cancelled) | ≤ 2 minutes | Event stream (Kafka/SQS topic) → marketing data pipeline |
| **Promo application events** | ≤ 2 minutes | Same event stream |
| **Loyalty points balance** | ≤ 5 minutes after rental completion | Async job triggered by booking state change |
| **Campaign performance aggregates** | Hourly refresh for dashboards; daily reconciliation | Scheduled ETL job |
| **Customer consent/opt-out** | ≤ 15 minutes propagation to all sending channels | Priority queue; acknowledged before next campaign send |
| **Live campaign dashboards** | Near-real-time (≤ 5-minute lag) | Streaming aggregation (e.g., Apache Flink or Spark Structured Streaming) |

---

## 10. Customer Consent & Data Privacy

**Interview question 10 — Consent and data privacy**

### 10.1 Consent Model

| Channel | Opt-in type | Opt-out mechanism |
|---|---|---|
| Email — marketing | Explicit opt-in at registration; double opt-in for GDPR regions | One-click unsubscribe link in every email; preference centre |
| SMS — marketing | Explicit opt-in (separate checkbox); TCPA-compliant for US | Reply STOP; auto-honoured within 15 minutes |
| Push notifications | Device-level OS permission + in-app preference toggle | Toggle in app settings |
| In-app messages | Implicit (no separate opt-in required by law for in-app) | In-app preference centre |
| Third-party ad targeting | Consent under GDPR/CCPA; cookie banner for web | Consent management platform (CMP) integration |

### 10.2 Frequency Caps

| Channel | Default frequency cap |
|---|---|
| Email | Maximum 3 marketing emails per customer per week |
| SMS | Maximum 2 marketing SMS per customer per week |
| Push | Maximum 1 promotional push per day |

Frequency caps are enforced centrally in the marketing orchestration layer before any message is dispatched.

### 10.3 Consent Storage

- All consent events must be stored with: customer ID, channel, consent status, timestamp, source (UI page / API call), IP address (hashed), and version of the privacy policy acknowledged.
- Consent records must be **immutable** (append-only log).

---

## 11. Marketing Channels

**Interview question 11 — Campaign channels**

| Channel | Use Cases | Integration |
|---|---|---|
| **Email** | Welcome sequences, booking confirmations, post-rental surveys, promotional campaigns | Mailchimp / Klaviyo |
| **SMS** | Time-sensitive offers, booking reminders, OTP | Twilio |
| **Push (mobile app)** | Real-time promo alerts, loyalty milestone notifications | Firebase Cloud Messaging |
| **In-app messages** | Contextual banners during booking flow, loyalty status updates | Mobile SDK (e.g., Braze / in-house) |
| **Third-party display/social ads** | Retargeting, lookalike audiences, awareness campaigns | Meta Ads API, Google Ads |
| **Owned web (on-site banners)** | Homepage promotions, booking-funnel offer injection | CMS-driven or feature-flag controlled |

---

## 12. A/B Testing

**Interview question 12 — A/B testing capabilities**

### 12.1 Scope

A/B testing is required for:
- Promotional offer creative (headline, discount value, imagery).
- Pricing display formats (e.g., "Save £20" vs. "20% off").
- Email subject lines and send times.
- Landing page layouts for campaigns.
- Loyalty enrollment CTA copy.

### 12.2 Experimentation Workflow

```
1. Marketer drafts experiment (hypothesis, variants A/B, split ratio, success metric, minimum run duration).
2. Marketing Manager reviews and approves experiment.
3. Engineering/Data team validates traffic split implementation (feature flag or holdout group).
4. Experiment runs for agreed duration (minimum 2 weeks or statistically significant sample).
5. Results reviewed in analytics dashboard (conversion lift, significance p-value, confidence interval).
6. Winner variant promoted to 100% traffic by marketing manager; experiment closed in system.
7. Results and methodology stored in experiment log (audit trail).
```

### 12.3 Technical Requirements

- Traffic splitting via feature flag service (e.g., LaunchDarkly, Unleash) or server-side holdout groups.
- Experiment assignments must be deterministic per customer (same customer always in same variant for duration of experiment).
- Assignment and conversion events must be emitted to the analytics pipeline.
- Minimum detectable effect and required sample size calculator available to marketers in the UI.

---

## 13. Campaign Workflow — Creation, Approval & Scheduling

**Interview question 13 — Campaign creation, approval, and scheduling**

### 13.1 Roles

| Role | Permissions |
|---|---|
| **Marketing Analyst** | Create and edit draft campaigns/promotions; run reports |
| **Marketing Manager** | All Analyst permissions + submit for approval, pause/resume live campaigns, approve A/B experiments |
| **Marketing Director** | All Manager permissions + final approval for campaigns above budget threshold (e.g., > £5,000 discount exposure) |
| **Finance Approver** | Review and approve budget impact; view-only on campaign config |
| **Compliance Officer** | Review consent/regulatory compliance for campaigns targeting GDPR/CCPA regions; approve or reject |
| **System Administrator** | Manage roles, integrations, API keys |

### 13.2 Campaign Lifecycle

```
DRAFT → PENDING_APPROVAL → APPROVED → SCHEDULED → ACTIVE → PAUSED (optional) → COMPLETED / CANCELLED
```

| Transition | Triggered by | Condition |
|---|---|---|
| DRAFT → PENDING_APPROVAL | Marketing Manager submits | All mandatory fields complete |
| PENDING_APPROVAL → APPROVED | Marketing Director (and Compliance Officer if applicable) approves | No open objections |
| PENDING_APPROVAL → DRAFT | Reviewer rejects with comments | — |
| APPROVED → SCHEDULED | System (cron) or manual scheduling | Scheduled start date set |
| SCHEDULED → ACTIVE | System at start_date | — |
| ACTIVE → PAUSED | Marketing Manager | — |
| ACTIVE / PAUSED → COMPLETED | System at end_date | — |
| Any → CANCELLED | Marketing Director | — |

### 13.3 Audit Trail

Every state transition and field change must be logged with: actor (user ID + role), timestamp, previous value, new value. Logs must be immutable, retained for a minimum of **3 years**, and exportable as CSV.

---

## 14. Personalization & Predictive Offers

**Interview question 14 — Personalization**

### 14.1 Scope for MVP

MVP will support **rule-based personalization** (no ML model required at launch):

| Trigger | Offer | Example |
|---|---|---|
| Customer has not rented in 90 days | Win-back discount (10%) | "We miss you — 10% off your next rental" |
| Customer's loyalty tier upgrade is imminent (< 200 pts away) | Double-points weekend prompt | "You're almost Gold — earn 2× this weekend" |
| Customer's last rental was in a specific vehicle category | Category-specific discount on next rental | "Book another SUV and save 15%" |
| Customer's rental region matches an active geofenced promo | In-app banner with promo code | — |
| Customer abandons booking at payment step | Cart-abandonment email with same offer | Sent after 2-hour delay |

### 14.2 Post-MVP (Phase 2)

Phase 2 will introduce ML-driven propensity models:

- **Inputs:** rental history, search history, loyalty tier, engagement score, segment membership, seasonal signals.
- **Outputs:** ranked list of offers with predicted conversion probability; best send-time recommendation per customer.
- **Infrastructure:** model serving via REST endpoint; predictions refreshed daily (batch); real-time inference for booking-flow personalisation.

---

## 15. Customer Data Lifecycle & Retention Policies

**Interview question 15 — Customer data lifecycle**

| Data Category | Retention Period | Deletion Trigger |
|---|---|---|
| Active customer profile | Duration of customer relationship + 2 years | Customer deletion request (right to erasure) or account closure |
| Consent records | 7 years (regulatory requirement) | Non-deletable; pseudonymisation after 7 years |
| Booking and transaction data | 7 years (financial record-keeping requirement) | Non-deletable during legal hold; pseudonymised on erasure request |
| Marketing interaction logs (opens, clicks) | 2 years | Rolling deletion job |
| A/B experiment assignments | 1 year after experiment close | Automated purge |
| Customer support communications | 3 years | Automated purge |

**Right-to-erasure workflow:**
1. Customer submits erasure request (in-app, email, or via data@company address).
2. System creates a deletion task ticket.
3. Automated job pseudonymises PII across all internal systems within **30 days**.
4. Third-party integrations (CRM, email platform) receive deletion API call within **30 days**.
5. Confirmation sent to customer; deletion log retained (pseudonymous) for compliance evidence.

---

## 16. Regulatory & Brand Guidelines

**Interview question 16 — Regulatory and brand constraints**

### 16.1 Regulatory Requirements

| Regulation | Applicability | Key Constraints |
|---|---|---|
| **GDPR** (EU/UK) | All customers in EU/UK | Lawful basis for processing; explicit opt-in for marketing; right to erasure; DPA required for processors |
| **CCPA** | California residents | Right to opt out of sale; disclosure of data categories; do-not-sell link |
| **TCPA** | US SMS | Written prior express consent for promotional SMS; STOP must be honoured |
| **CAP Code / ASA** (UK) | UK marketing communications | No misleading claims; price claims must include all mandatory charges |
| **PSD2 / financial promotions** | If rental involves financing or deposits | Regulated financial promotion rules may apply; legal review required |

### 16.2 Brand Guidelines

- All customer-facing campaign assets must use the approved brand colour palette, typography, and tone of voice defined in the Brand Style Guide (link TBD).
- Promotional copy must be reviewed by the brand team before scheduling.
- Campaign names and offer descriptions must not reference competitor brands.

---

## 17. Reporting & Export Capabilities

**Interview question 17 — Reporting and export**

### 17.1 Standard Reports

| Report | Frequency | Audience |
|---|---|---|
| Campaign performance summary | Daily (auto-sent) + on-demand | Marketing Manager, Director |
| Promo code redemption report | On-demand + weekly digest | Marketing Analyst |
| Loyalty programme health (active members, tier distribution, points liability) | Weekly | Marketing Director, Finance |
| Channel attribution report | Monthly + on-demand | Marketing Director |
| Customer consent / opt-out summary | Monthly | Compliance Officer |
| A/B test results report | On experiment close | Marketing team |

### 17.2 Export Formats

- **CSV** for all tabular reports (UTF-8 encoded).
- **PDF** for formatted executive summaries.
- **JSON/Parquet** for raw data export to data warehouse.
- Scheduled email delivery of reports to defined recipient lists.
- Self-service dashboard (read-only) accessible to all marketing roles.

### 17.3 Dashboard Requirements

- Real-time campaign metrics (impressions, sends, opens, clicks, conversions, revenue).
- Drill-down by campaign, channel, segment, date range, and location.
- Anomaly highlighting (e.g., CTR drops > 20% vs. prior period).

---

## 18. Alerts & Automated Notifications

**Interview question 18 — Alerts and automated notifications**

| Alert | Trigger Condition | Recipients | Channel |
|---|---|---|---|
| **Budget cap warning** | Promo discount exposure reaches 80% of budget | Marketing Manager, Finance Approver | Email + in-app |
| **Budget cap reached** | 100% of budget consumed | Marketing Manager, Finance Approver | Email + SMS |
| **Promo abuse detected** | Abuse detection rules triggered (see §19) | Marketing Manager, Security team | Email + in-app |
| **Campaign performance anomaly** | CTR or conversion drops > 30% vs. prior period for ≥ 2 hours | Marketing Analyst, Manager | Email |
| **Campaign go-live confirmation** | Campaign transitions to ACTIVE | Campaign owner | Email |
| **Campaign expiry warning** | 24 hours before scheduled end_date | Campaign owner | Email |
| **Integration failure** | CRM / email platform API returns errors for > 5 minutes | System Administrator | PagerDuty / email |
| **Consent sync delay** | Opt-out not propagated to all channels within 15 minutes | System Administrator | PagerDuty |
| **Loyalty tier change** | Customer tier upgraded or downgraded | Customer (transactional) | Email + push |

---

## 19. Promotional Abuse & Misuse Detection

**Interview question 19 — Handling promotional abuse**

### 19.1 Abuse Patterns to Detect

| Pattern | Detection Rule |
|---|---|
| **Code sharing / bulk redemption** | Same promo code used by > N distinct customer accounts within a configurable time window |
| **Self-referral** | Referrer and referee share the same device ID, email domain, or payment method |
| **Account churning** | Multiple new accounts created from the same IP address or device fingerprint to exploit new-customer offers |
| **Velocity abuse** | Single customer applies > N promo codes within 24 hours |
| **Synthetic bookings** | Booking created and cancelled immediately after promo credit is applied |

### 19.2 Response Actions

| Severity | Action |
|---|---|
| **Low** | Flag for manual review; allow transaction to proceed; log event |
| **Medium** | Hold promo credit pending review; notify Marketing Manager; send internal alert |
| **High** | Block promo application; notify Security team; optionally suspend account pending investigation |

### 19.3 Safeguards

- All abuse detection actions are logged with reason code for audit and potential customer appeal.
- Customers incorrectly flagged must be able to appeal via customer support; credits restored within 48 hours upon resolution.
- Abuse thresholds are configurable by Marketing Manager; changes are audit-logged.

---

## 20. MVP Acceptance Criteria

**Interview question 20 — Successful MVP marketing capability**

The MVP is considered successful when all of the following criteria are met:

| # | Acceptance Criterion | Validation Method |
|---|---|---|
| AC-1 | A Marketing Manager can create, configure, and activate a discount-code promotion with eligibility rules, time windows, and usage limits. | Manual UAT + automated integration test |
| AC-2 | Promotion codes are correctly applied at booking checkout, respecting stacking and eligibility rules. | Automated test suite covering ≥ 10 rule combinations |
| AC-3 | The system is integrated with at least one CRM platform (Salesforce or HubSpot), with bidirectional sync of customer profile and booking events. | Integration smoke test; data verified in CRM sandbox |
| AC-4 | Customer loyalty points are earned and displayed in the customer account within 24 hours of rental completion. | End-to-end test with test booking |
| AC-5 | Basic campaign performance metrics (sends, opens, clicks, conversions) are visible in a marketing dashboard. | Dashboard demo with seeded data |
| AC-6 | Opt-out requests are propagated to all sending channels within 15 minutes. | Automated end-to-end test |
| AC-7 | All promotion creation and approval actions are recorded in an immutable audit log. | Log inspection test |
| AC-8 | The system detects and blocks the top two abuse patterns (code sharing, self-referral) in a staging environment test. | Security test scenario |
| AC-9 | Campaign data is exportable as CSV for at least three standard report types. | Manual export verification |
| AC-10 | All GDPR consent requirements are satisfied as verified by the Compliance Officer sign-off. | Compliance checklist review |

---

## 21. Engineering & Operational Considerations

### 21.1 SLAs for Data Freshness & API Availability

| Component | SLA |
|---|---|
| Booking event pipeline (Kafka/SQS) | 99.9% availability; ≤ 2-minute end-to-end latency at p95 |
| CRM sync API | 99.5% availability; retry within 5 minutes on failure |
| Email/SMS sending API | 99.9% availability; bounce-back handling within 1 hour |
| Marketing dashboard read API | 99.5% availability; ≤ 3-second response at p99 |
| Consent propagation | Opt-out honoured across all channels within 15 minutes (hard SLA) |

### 21.2 Data Privacy & Compliance

- All PII in transit must be encrypted (TLS 1.2+).
- All PII at rest must be encrypted (AES-256 or equivalent).
- Consent state must be checked by the orchestration layer **before** any outbound message is dispatched.
- GDPR Data Processing Agreements must be in place with all third-party marketing platforms before integration goes live.
- A Data Protection Impact Assessment (DPIA) must be completed prior to launch of any behavioural targeting or ML personalisation feature.

### 21.3 Throughput Expectations

| Workload | Expected Peak Volume |
|---|---|
| Campaign email sends | 500,000 emails / hour during peak campaign launch |
| SMS sends | 50,000 SMS / hour |
| Push notifications | 200,000 pushes / hour |
| Inbound webhook events (CRM, ad platforms) | 1,000 events / minute |
| Booking events for marketing pipeline | 10,000 events / minute (black-friday peak) |

Sending must be rate-limited and back-pressured to respect third-party API rate limits without dropping messages.

### 21.4 Audit Logging & Traceability

- All campaign state changes, promotion modifications, role assignments, and abuse-detection actions must be logged to an append-only audit store.
- Audit log schema: `{ event_id, timestamp, actor_id, actor_role, entity_type, entity_id, action, previous_state, new_state, metadata }`.
- Audit logs must be retained for a minimum of **3 years** and must be queryable by entity, actor, and date range.
- Audit logs must not be modifiable or deletable by any application-level user (enforced at the storage layer).

---

## 22. Open Questions & Assumptions

| # | Open Question | Owner | Target Resolution |
|---|---|---|---|
| OQ-1 | Which CRM platform takes priority for Phase 1 integration: Salesforce or HubSpot? | Marketing Director | Sprint 1 kick-off |
| OQ-2 | What is the exact budget threshold that triggers Finance Approver review? | Finance | Sprint 1 kick-off |
| OQ-3 | Are there existing brand guidelines documented, and where are they hosted? | Brand / Marketing | Sprint 1 kick-off |
| OQ-4 | Which consent management platform (CMP) will be used for web cookie consent? | Legal / Engineering | Sprint 2 |
| OQ-5 | Will the mobile app be iOS-only, Android-only, or cross-platform at launch? | Engineering | Architecture review |
| OQ-6 | Is a third-party loyalty platform (e.g., Antavo, Yotpo) preferred, or will loyalty be built in-house? | Product / Engineering | Sprint 1 kick-off |
| OQ-7 | Are US customers in scope for MVP (TCPA / CCPA compliance)? | Legal | Sprint 1 kick-off |
| OQ-8 | What BI/data warehouse tool is available for marketing reporting (Looker, Tableau, Power BI)? | Engineering / Data | Sprint 2 |

**Assumptions made in this document (pending confirmation):**
- GDPR (EU/UK) applies from day one; CCPA assumed in-scope for caution.
- Salesforce and Mailchimp/Klaviyo are the primary integration targets for MVP.
- The booking system exposes a reliable event stream that marketing can subscribe to.
- Budget for Phase 1 ML personalisation is not approved; rule-based personalisation only at MVP.
