# Product Requirement Document — Marketing Module
## Car Rental System

| Field            | Detail                              |
|------------------|-------------------------------------|
| **Document ID**  | PRD-MKT-001                         |
| **Version**      | 1.0                                 |
| **Status**       | Draft                               |
| **Author**       | Product Owner                       |
| **Date**         | 2026-03-07                          |
| **Business Line**| Car Rental (new)                    |

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Business Objectives](#2-business-objectives)
3. [Target Customer Segments](#3-target-customer-segments)
4. [Functional Requirements](#4-functional-requirements)
   - 4.1 [Promotions & Campaign Management](#41-promotions--campaign-management)
     - 4.1.1 [Promotional Types](#411-promotional-types)
     - 4.1.2 [Promotional Rule Engine](#412-promotional-rule-engine)
     - 4.1.3 [Campaign Workflow](#413-campaign-workflow)
   - 4.2 [Loyalty Program](#42-loyalty-program)
     - 4.2.1 [Tiered Program Structure](#421-tiered-program-structure)
     - 4.2.2 [Points Earning Rules](#422-points-earning-rules)
     - 4.2.3 [Points Redemption Rules](#423-points-redemption-rules)
   - 4.3 [Customer Segmentation](#43-customer-segmentation)
   - 4.4 [CRM & Platform Integrations](#44-crm--platform-integrations)
   - 4.5 [Marketing Analytics & KPIs](#45-marketing-analytics--kpis)
   - 4.6 [Data Freshness & Reporting](#46-data-freshness--reporting)
   - 4.7 [Communication Channels & Customer Consent](#47-communication-channels--customer-consent)
     - 4.7.1 [Supported Channels](#471-supported-channels)
     - 4.7.2 [Consent Management](#472-consent-management)
   - 4.8 [A/B Testing](#48-ab-testing)
   - 4.9 [Personalization](#49-personalization)
   - 4.10 [Promotional Abuse Detection](#410-promotional-abuse-detection)
   - 4.11 [Alerts & Automated Notifications (Internal)](#411-alerts--automated-notifications-internal)
5. [Non-Functional Requirements](#5-non-functional-requirements)
   - 5.1 [Data Privacy & Compliance](#51-data-privacy--compliance)
   - 5.2 [Brand Guidelines](#52-brand-guidelines)
   - 5.3 [Performance & SLAs](#53-performance--slas)
   - 5.4 [Audit Logging & Traceability](#54-audit-logging--traceability)
   - 5.5 [Data Lifecycle & Retention](#55-data-lifecycle--retention)
6. [Acceptance Criteria — MVP Marketing Capability](#6-acceptance-criteria--mvp-marketing-capability)
7. [Out of Scope (MVP)](#7-out-of-scope-mvp)
8. [Assumptions & Dependencies](#8-assumptions--dependencies)
9. [Open Questions](#9-open-questions)
10. [Revision History](#10-revision-history)

---

## 1. Executive Summary

Our company is expanding from car sales into car rental as a new line of business. This Product Requirement Document defines the functional and non-functional requirements for the **Marketing Module** of the Car Rental System. It is based on structured stakeholder interviews conducted during the requirement-analysis phase. The module enables the business to attract and retain rental customers through promotions, loyalty programs, targeted campaigns, and data-driven personalization — while complying with data-privacy regulations and brand guidelines.

---

## 2. Business Objectives

The marketing module must support the following primary objectives (in priority order):

| Priority | Objective                   | Description                                                                 |
|----------|-----------------------------|-----------------------------------------------------------------------------|
| 1        | Customer Acquisition        | Drive new rental customers through digital and traditional channels.        |
| 2        | Revenue per Customer        | Increase basket size and rental frequency with targeted offers.             |
| 3        | Customer Retention          | Reduce churn through loyalty programs, personalization, and timely re-engagement. |
| 4        | Brand Awareness             | Establish the car rental brand identity distinct from the existing car sales brand. |

---

## 3. Target Customer Segments

The following customer segments are prioritized for the initial launch, in the order below:

1. **Business Travelers** — Frequent short-duration rentals, typically booked via corporate accounts or business travel portals. Price-sensitive on total cost, time-sensitive on booking speed.
2. **Tourists** — Seasonal demand, often price-comparing, may require add-ons (insurance, GPS, child seats). Destination-specific campaigns apply.
3. **Local Short-Term Renters** — Individuals who need a vehicle for personal errands, weekend trips, or temporary transport. Responsive to proximity-based promotions.
4. **Long-Term Corporate Accounts** — Enterprises that require fleets for employee use. Require dedicated account management, volume discounts, and consolidated billing.

---

## 4. Functional Requirements

### 4.1 Promotions & Campaign Management

#### 4.1.1 Promotional Types

The system must support the following promotion types:

| Type                | Description                                                                 |
|---------------------|-----------------------------------------------------------------------------|
| Discount Codes      | Alphanumeric codes that apply a percentage or flat-value discount at checkout. |
| Seasonal Campaigns  | Time-bound promotions tied to holidays, school breaks, or travel seasons.   |
| Bundles             | Combined offers (e.g., rental + insurance, rental + fuel pre-fill) at a reduced combined price. |
| Loyalty Points      | Points earned per rental that can be redeemed for future discounts or upgrades. |
| Referral Bonuses    | Credits awarded to both referrer and referee upon the referee's first completed rental. |

#### 4.1.2 Promotional Rule Engine

Promotional rules must be configurable without code deployments. The rule engine must support:

| Rule Dimension        | Requirement                                                                 |
|-----------------------|-----------------------------------------------------------------------------|
| **Stacking Rules**    | Define whether multiple promotions can be combined. At minimum: (a) no stacking, (b) best-deal wins, (c) explicit allow-list of stackable promotion pairs. |
| **Eligibility**       | Rules may restrict by customer segment, account type, rental history, loyalty tier, geographic location, or vehicle category. |
| **Time Windows**      | Each promotion has a mandatory start date/time and end date/time. Time-zone-aware scheduling is required. |
| **Geofencing**        | Promotions may be restricted to customers whose registered address, pickup location, or device location falls within a defined geographic region or city. |
| **Usage Limits**      | Per-promotion total-use cap and per-customer use cap.                       |
| **Minimum Conditions**| Minimum rental duration, minimum spend, or minimum advance-booking window. |

#### 4.1.3 Campaign Workflow

Campaigns must follow a structured lifecycle:

```
Draft → Pending Approval → Approved → Scheduled → Active → Completed / Cancelled
```

Roles and responsibilities:

| Role                 | Permissions                                                                  |
|----------------------|------------------------------------------------------------------------------|
| Marketing Analyst    | Create and edit campaigns in Draft status.                                   |
| Marketing Manager    | Submit for approval, edit, and cancel campaigns.                             |
| Approver (Director+) | Approve or reject campaigns; add approval notes.                             |
| System Administrator | Override status; manage rule-engine configuration.                           |

Requirements:
- Full audit trail: every status change, edit, and approval action must be logged with timestamp and actor identity.
- Scheduled campaigns must activate and deactivate automatically at the configured times.
- Cancelled campaigns must automatically invalidate any unused promo codes.

---

### 4.2 Loyalty Program

#### 4.2.1 Tiered Program Structure

The loyalty program must support multiple status tiers. Suggested tiers (configurable by admins):

| Tier      | Entry Criteria (example)             | Key Benefits                               |
|-----------|--------------------------------------|--------------------------------------------|
| Bronze    | Default (all registered customers)  | Earn base points; birthday discount.       |
| Silver    | 5+ rentals or 1,000+ points/year    | 1.25× point multiplier; priority support. |
| Gold      | 15+ rentals or 5,000+ points/year   | 1.5× multiplier; free upgrades; dedicated account manager. |
| Platinum  | 30+ rentals or 15,000+ points/year  | 2× multiplier; lounge access; complimentary insurance. |

Tiers are re-evaluated quarterly. Downgrades must include a 30-day grace period with advance notice.

#### 4.2.2 Points Earning Rules

- Base earn rate: configurable points per unit of spend (e.g., 1 point per $1 spent).
- Bonus multipliers per tier (see §4.2.1).
- Bonus point events: first rental, referring a friend, completing a profile, anniversary rental.
- Points expire after 12 months of account inactivity.

#### 4.2.3 Points Redemption Rules

- Minimum redemption threshold: configurable (e.g., 500 points).
- Redemption value: configurable conversion rate (e.g., 100 points = $1 discount).
- Redemption limited to one points redemption per transaction (may stack with one promotional code, subject to stacking rules).
- Points cannot be transferred between accounts.

---

### 4.3 Customer Segmentation

The system must capture and maintain the following customer attributes to enable segmentation:

| Attribute              | Source                         | Use Case                              |
|------------------------|--------------------------------|---------------------------------------|
| Age / Date of Birth    | Registration                   | Age-based eligibility; young-driver surcharge targeting. |
| Geographic Location    | Registration + GPS (optional)  | Geofenced promotions; regional campaigns. |
| Rental Frequency       | Transaction history            | Loyalty tier calculation; re-engagement triggers. |
| Vehicle Preference     | Booking history                | Personalized vehicle recommendations and offers. |
| Preferred Pickup Locations | Booking history           | Location-specific promotions.         |
| Last Rental Date       | Transaction history            | Churn detection; win-back campaigns.  |
| Corporate Account Flag | Registration / Account type    | B2B vs. B2C campaign routing.         |
| Channel Source         | UTM parameters / Referral data | Channel attribution analysis.         |
| Consent Flags          | Registration + preference center | Opt-in/opt-out per channel and purpose. |

Segments must be definable as dynamic rules (e.g., "customers who have not rented in 90 days and are Gold tier") that resolve at campaign execution time.

---

### 4.4 CRM & Platform Integrations

The system must integrate with the following external platforms:

| Platform Type             | Integration Requirement                                                   |
|---------------------------|---------------------------------------------------------------------------|
| CRM (e.g., Salesforce, HubSpot) | Bidirectional sync of customer profiles, segment membership, and interaction history via REST API. |
| Email Service Provider (e.g., SendGrid, Mailchimp) | Campaign send, delivery status webhooks, unsubscribe sync. |
| SMS Gateway (e.g., Twilio, Vonage) | Transactional and promotional SMS sends; delivery receipt callbacks. |
| Push Notification Service (e.g., Firebase FCM, Apple APNs) | Mobile push for in-app promotional messages. |
| Ad Platforms (e.g., Google Ads, Meta) | Audience export for custom-audience targeting; conversion pixel/event tracking. |
| Analytics Platform (e.g., Google Analytics 4, Mixpanel) | Event streaming for campaign attribution and funnel analysis. |

All integrations must:
- Use authenticated connections (OAuth 2.0 or API key) with secrets stored in a secrets manager.
- Provide a health-check endpoint or status indicator visible to system administrators.
- Support retry with exponential back-off for transient failures.
- Log all outbound API calls and inbound webhooks for audit and debugging.

---

### 4.5 Marketing Analytics & KPIs

The marketing analytics dashboard must expose the following KPIs:

| KPI                          | Definition                                                                 |
|------------------------------|----------------------------------------------------------------------------|
| Conversion Rate              | Bookings completed / campaign impressions or clicks.                      |
| Promotional ROI              | (Revenue attributable to promo − promo discount cost) / promo discount cost. |
| Customer Lifetime Value (LTV)| Projected revenue over expected customer tenure.                          |
| Churn Rate                   | % of active customers who did not rent in a rolling 90-day window.        |
| Channel Attribution          | Revenue and bookings attributed per acquisition channel using last-touch and multi-touch models. |
| Cost per Acquisition (CPA)   | Marketing spend / new customers acquired per campaign.                    |
| Redemption Rate              | Promo codes redeemed / promo codes issued.                                |
| Loyalty Points Liability     | Total outstanding points balance across all customers (financial exposure).|
| Campaign Engagement Rate     | Opens, clicks, or interactions / messages delivered.                      |

---

### 4.6 Data Freshness & Reporting

| Context                    | Freshness Requirement                                          |
|----------------------------|----------------------------------------------------------------|
| Executive Dashboards       | Daily refresh (by 06:00 local time).                          |
| Campaign Performance Dashboards | Hourly refresh during active campaigns.                 |
| Operational Alerts         | Near-real-time (≤ 15 minutes lag).                            |
| Data Warehouse / Historical | Daily batch load; full history retained per data lifecycle policy (§4.9). |

Reporting export capabilities:
- CSV and Excel export for all dashboard views.
- Scheduled report delivery via email (daily / weekly / monthly cadences, configurable).
- REST API endpoint for programmatic data access by BI tools.

---

### 4.7 Communication Channels & Customer Consent

#### 4.7.1 Supported Channels

| Channel        | Use                                               |
|----------------|---------------------------------------------------|
| Email          | Campaign newsletters, promotional offers, booking confirmations with upsell. |
| SMS            | Time-sensitive short promotions, OTP, booking reminders. |
| Push (Mobile)  | In-app personalized offers, loyalty point updates. |
| In-App Banners | Contextual promotions within the rental booking flow. |
| Third-Party Ads | Retargeting and lookalike audience campaigns.    |

#### 4.7.2 Consent Management

- Customers must explicitly opt in to each marketing channel at registration or via an accessible preference center.
- Opt-out requests must be honored within 24 hours across all channels.
- Frequency caps must be configurable per channel and per campaign type (e.g., max 2 promotional emails per week per customer).
- Consent history (what was consented, when, by which action) must be stored as an immutable audit log.
- Transactional messages (booking confirmations, receipts) are not subject to marketing opt-in requirements.

---

### 4.8 A/B Testing

The system must support controlled experimentation for offers and prices:

- Define experiment variants (e.g., Variant A: 10% discount code vs. Variant B: free upgrade offer).
- Randomly assign customers to variants at campaign execution, with configurable split ratios.
- Statistical significance tracking with configurable confidence threshold (default: 95%).
- Experiment results dashboard showing conversion rate, revenue, and significance per variant.
- Winning variant can be promoted to a full campaign without rebuilding from scratch.
- Experiments must be tagged in the audit log and in all downstream analytics events.

---

### 4.9 Personalization

The system must support the following personalization capabilities:

| Capability                      | Input                              | Output                                      |
|---------------------------------|------------------------------------|---------------------------------------------|
| Dynamic offer content           | Customer segment, vehicle preference, rental history | Personalized email/push body with relevant vehicle category and offer. |
| Predictive next-rental offer    | Last rental date, frequency model  | Re-engagement campaign triggered N days before predicted next rental. |
| Location-based recommendations  | Last pickup location, home city    | Promotion featuring nearby rental station and relevant vehicle class. |
| Upgrade recommendations         | Current booking vehicle class, loyalty tier | Upsell banner suggesting a higher vehicle class with loyalty points incentive. |

Personalization logic must be reviewable and overridable by the marketing team without engineering involvement.

---

### 4.10 Promotional Abuse Detection

The system must detect and mitigate promotional misuse:

| Abuse Pattern                        | Detection Mechanism                                                        | Response                                                 |
|--------------------------------------|----------------------------------------------------------------------------|----------------------------------------------------------|
| Same promo code used by multiple accounts sharing identity signals (IP, device, payment) | Velocity check on code redemptions across linked identity attributes | Flag for manual review; optionally block redemption above threshold. |
| Bulk account creation to exploit referral bonus | Registration velocity check; email domain analysis                  | Require additional verification; alert marketing ops team. |
| Promo code sharing beyond intended audience | Usage-count enforcement and per-customer caps                          | Hard block once cap is reached.                          |
| Fake referrals (self-referral via aliases) | Email/phone deduplication on referral pair matching                   | Disqualify bonus; flag account.                          |

Alerts must be sent to the Marketing Operations team when abuse thresholds are breached. All flagged events must be logged with full context for investigation.

---

### 4.11 Alerts & Automated Notifications (Internal)

The system must send automated alerts to marketing stakeholders for the following events:

| Alert                        | Trigger                                                        | Recipients             |
|------------------------------|----------------------------------------------------------------|------------------------|
| Campaign going live          | Campaign transitions to Active status                         | Campaign owner         |
| Budget cap approaching       | 80% of campaign budget consumed                               | Marketing Manager      |
| Budget cap reached           | 100% of budget consumed; campaign auto-paused                 | Marketing Manager + Director |
| Promo abuse threshold breach | Abuse detection rule triggered (§4.10)                        | Marketing Ops team     |
| Campaign under-performance   | Conversion rate > 30% below forecast after 24 hours           | Marketing Analyst + Manager |
| Data integration failure     | External platform webhook or API unavailable for > 15 minutes | System Admin + Marketing Ops |
| Loyalty point liability spike | Outstanding points balance increases > 20% week-over-week   | Finance + Marketing Director |

---

## 5. Non-Functional Requirements

### 5.1 Data Privacy & Compliance

| Requirement                              | Detail                                                                 |
|------------------------------------------|------------------------------------------------------------------------|
| GDPR (EU customers)                      | Lawful basis for processing must be recorded per customer. Right to erasure must delete or anonymize marketing data within 30 days of verified request. |
| CCPA (California customers)              | "Do Not Sell My Personal Information" opt-out must be supported and honored within 15 business days. |
| Consent audit log                        | Immutable, timestamped record of all consent actions. Retained for 7 years. |
| Data minimization                        | Only attributes necessary for the defined marketing purpose are collected and processed. |
| Third-party data sharing                 | Customers must be informed of any data shared with ad platforms. Data-sharing agreements must be in place before integration goes live. |

### 5.2 Brand Guidelines

- All marketing communications must use approved brand assets (logos, color palette, typography) as defined in the Brand Style Guide.
- Communications must clearly differentiate the car rental brand from the existing car sales brand.
- Legal disclaimers required by regional regulators must be included in all promotional communications.

### 5.3 Performance & SLAs

| Metric                              | Target                                                     |
|-------------------------------------|------------------------------------------------------------|
| Promotional code validation latency | ≤ 200 ms (p99) at checkout                                |
| Campaign activation time            | Campaigns must activate within 5 minutes of scheduled start time. |
| Email/SMS delivery throughput       | Up to 500,000 messages per campaign send batch.           |
| Webhook handling throughput         | Up to 1,000 inbound events per second (delivery receipts, unsubscribes). |
| CRM sync API availability           | 99.9% monthly uptime SLA for integration endpoints.       |
| Analytics data API availability     | 99.5% monthly uptime SLA.                                 |

### 5.4 Audit Logging & Traceability

- All campaign CRUD operations, status transitions, approval decisions, and rule-engine configuration changes must be logged.
- Logs must include: actor identity, timestamp (UTC), action type, before-state, after-state.
- Audit logs must be immutable and retained for a minimum of 3 years.
- Logs must be searchable and exportable by authorized compliance officers.

### 5.5 Data Lifecycle & Retention

| Data Type                       | Retention Period                               |
|---------------------------------|------------------------------------------------|
| Customer marketing profile      | Duration of customer relationship + 3 years   |
| Consent history                 | 7 years from consent action                   |
| Campaign configuration & results | 5 years from campaign end date               |
| Audit logs                      | 3 years minimum (7 years for consent events)  |
| A/B test results                | 5 years from experiment end                   |
| Anonymized analytics data       | Indefinite (no PII retained after anonymization) |

---

## 6. Acceptance Criteria — MVP Marketing Capability

The minimum viable product for the Marketing Module is considered complete when all of the following criteria are met:

| # | Acceptance Criterion                                                                                                  |
|---|-----------------------------------------------------------------------------------------------------------------------|
| 1 | A marketing analyst can create a discount-code promotion with eligibility rules, a time window, and a usage cap, and submit it for approval. |
| 2 | An approver can approve or reject a submitted campaign, and the status is updated with an audit log entry.             |
| 3 | An approved campaign automatically activates at its scheduled start time and deactivates at its scheduled end time.    |
| 4 | A customer can enter a valid promotional code at checkout and receive the correct discount.                            |
| 5 | An invalid, expired, or over-limit promotional code is rejected with a clear error message.                            |
| 6 | Customer consent for email marketing is captured at registration and respected: opted-out customers do not receive marketing emails. |
| 7 | The system integrates with at least one email service provider and one CRM platform, with bidirectional sync of customer opt-out status. |
| 8 | A basic campaign performance dashboard is available showing: total sends, delivery rate, opens, clicks, conversions, and promo redemptions. |
| 9 | Marketing managers receive an automated alert when a campaign's budget cap reaches 80%.                               |
| 10| The loyalty points earning and basic redemption flow is operational for Bronze-tier customers.                         |

---

## 7. Out of Scope (MVP)

The following capabilities are acknowledged but deferred to future releases:

- Predictive personalization using machine-learning models.
- Full multi-touch attribution modeling.
- Third-party ad platform audience export.
- Platinum and Gold loyalty tier activation (Bronze and Silver only at MVP).
- A/B testing for price experiments (offer experiments only at MVP).

---

## 8. Assumptions & Dependencies

| Assumption / Dependency                                | Notes                                                        |
|--------------------------------------------------------|--------------------------------------------------------------|
| A customer identity and authentication service exists   | Marketing module will consume customer IDs from this service. |
| A booking and transaction service exists               | Loyalty points and conversion attribution depend on completed-booking events. |
| Brand Style Guide is available before UI development   | Required for email/SMS templates and in-app banners.         |
| Legal review of consent flows is completed before launch | Consent UX copy and disclaimers must be legally approved.  |
| CRM vendor selection is finalized before integration sprint | API credentials required for integration development.    |

---

## 9. Open Questions

| # | Question                                                                 | Owner                   | Due      |
|---|--------------------------------------------------------------------------|-------------------------|----------|
| 1 | What is the initial budget per promotional campaign for the pilot period?| Marketing Director      | TBD      |
| 2 | Which CRM platform is the confirmed primary system of record?            | IT / Marketing Director | TBD      |
| 3 | Are there existing loyalty program commitments from car sales that must carry over? | Product Owner  | TBD      |
| 4 | What regional markets will be live at launch (EU, US, APAC)?            | Business Development    | TBD      |
| 5 | Is there a legal entity structure that affects how consent is collected per region? | Legal / Compliance | TBD |

---

## 10. Revision History

| Version | Date       | Author         | Changes                     |
|---------|------------|----------------|-----------------------------|
| 1.0     | 2026-03-07 | Product Owner  | Initial draft based on stakeholder interview responses. |
