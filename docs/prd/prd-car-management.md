# PRD - Car Management

## Document Information

| Field | Details |
|---|---|
| **Product / Feature Name** | Car Management |
| **Author** | @copilot |
| **Date** | |
| **Version** | |

---

## Table of Contents

1. [Overview](#overview)
   - [Background](#background)
   - [Objective](#objective)
   - [Goals](#goals)
2. [Problem Statement](#problem-statement)
3. [Functional Requirements](#functional-requirements)
   - [FR-1: Vehicle Onboarding](#fr-1-vehicle-onboarding)
   - [FR-2: Vehicle Lifecycle Management](#fr-2-vehicle-lifecycle-management)
   - [FR-3: Insurance Expiry Block](#fr-3-insurance-expiry-block)
   - [FR-4: Vehicle Retirement](#fr-4-vehicle-retirement)
   - [FR-5: Home Location Assignment](#fr-5-home-location-assignment)
   - [FR-6: Location Transfer](#fr-6-location-transfer)
   - [FR-7: Real-Time Vehicle Availability](#fr-7-real-time-vehicle-availability)
   - [FR-8: Planned Vehicle Availability](#fr-8-planned-vehicle-availability)
   - [FR-9: GPS Tracking](#fr-9-gps-tracking)
   - [FR-10: Geofencing Alert](#fr-10-geofencing-alert)
   - [FR-11: Automatic Reservation Allocation](#fr-11-automatic-reservation-allocation)
   - [FR-12: Vehicle Substitution](#fr-12-vehicle-substitution)
   - [FR-13: Delivery & Pickup Scheduling](#fr-13-delivery--pickup-scheduling)
   - [FR-14: Proof of Handover Capture](#fr-14-proof-of-handover-capture)
   - [FR-15: Customer Identity Verification at Pickup](#fr-15-customer-identity-verification-at-pickup)
   - [FR-16: Return Inspection](#fr-16-return-inspection)
   - [FR-17: Damage Recording with Positional Mapping](#fr-17-damage-recording-with-positional-mapping)
   - [FR-18: Fuel Level Check on Return](#fr-18-fuel-level-check-on-return)
   - [FR-19: Maintenance Scheduling](#fr-19-maintenance-scheduling)
   - [FR-20: Maintenance Availability Block](#fr-20-maintenance-availability-block)
   - [FR-21: Damage Incident Reporting](#fr-21-damage-incident-reporting)
   - [FR-22: Repair Cost Estimation](#fr-22-repair-cost-estimation)
   - [FR-23: Fuel Charge Calculation](#fr-23-fuel-charge-calculation)
   - [FR-24: Utilization Dashboard](#fr-24-utilization-dashboard)
   - [FR-25: Consecutive Idle Alert](#fr-25-consecutive-idle-alert)
   - [FR-26: Rental Extension](#fr-26-rental-extension)
   - [FR-27: Early Return & Repositioning](#fr-27-early-return--repositioning)
   - [FR-28: Lost Key Charge](#fr-28-lost-key-charge)
   - [FR-29: Workforce Task Assignment](#fr-29-workforce-task-assignment)
4. [Non-Functional Requirements](#non-functional-requirements)
5. [Dependency & Constraints](#dependency--constraints)
6. [Success Metrics](#success-metrics)

---

## Overview

### Background

The company has an established car sales business and is expanding into car rental as a new line of business. There is no prior experience or existing system for car rental operations. The Car Management feature is a foundational operational module that covers the full lifecycle of a rental vehicle — from onboarding into the fleet through daily operations (pickup, delivery, maintenance, inspection) to decommissioning — ensuring that fleet assets are tracked, maintained, and made available accurately and efficiently.

### Objective

To build a centralized car management module that gives operations teams full control and visibility over the rental fleet, enabling accurate vehicle tracking, scheduled maintenance, streamlined pickup/delivery workflows, and reliable availability data to support reservations.

### Goals

- Enable fleet managers to register, track, and manage vehicles throughout their lifecycle.
- Ensure accurate real-time and planned vehicle availability for the reservation system.
- Support standardized pickup, delivery, and return inspection workflows for field crews.
- Automate maintenance scheduling and availability blocking to prevent releasing unserviceable vehicles.
- Provide utilization and idle analytics to support fleet performance decisions.
- Alert operations teams to critical events (geofencing violations, maintenance overdue).

---

## Problem Statement

As the company enters the car rental business, there is no operational system to manage the fleet. Without a dedicated car management module:

- There is no structured way to onboard vehicles, track their status, or manage transitions between states (e.g., available, in maintenance, decommissioned).
- Fleet managers have no visibility into vehicle locations, availability windows, or maintenance needs.
- Field crews lack standardized, digital workflows for vehicle handover, inspection, and damage documentation.
- Reservations cannot be fulfilled reliably due to the absence of accurate availability data.
- Insurance and maintenance obligations may be missed, exposing the company to legal and financial risk.

**Key users affected:**
- **Fleet Managers:** responsible for vehicle registration, lifecycle management, maintenance scheduling, and fleet analytics.
- **Operations Managers:** responsible for monitoring vehicle locations, geofencing compliance, and daily operational decisions.
- **Delivery/Pickup Crew:** responsible for vehicle handover, identity verification, and proof-of-delivery capture in the field.
- **Inspection Crew:** responsible for return inspections, damage documentation, and fuel level recording.
- **Supervisors:** responsible for assigning post-return tasks to field staff.

**Why this is important now:** The car rental business cannot launch without a fleet management foundation. All other modules — reservations, billing, customer management — depend on reliable fleet data that only this module provides.

---

## Functional Requirements

### FR-1: Vehicle Onboarding

**Title:** Register a New Vehicle into the Rental Fleet

**Statement:** **As a** fleet manager, **I want** to register a new vehicle with all required attributes, **so that** it is formally entered into the fleet and tracked from the point of acquisition.

**Requirement Detail:**

When adding a vehicle, the following information must be captured:
- VIN
- License plate number
- Purchase date
- Purchase cost
- Insurance details (insurer name, policy number, coverage dates)
- Odometer reading at acquisition
- Vehicle type details: brand, model, manufacturing year
- Vehicle size type: Small (4-seat) or Medium (7-seat) — defines the physical size and seating capacity
- Vehicle class: Economy or Luxury — defines the tier/quality level independent of size
- Number of seats
- Fuel type: Gas, Electric, or Hybrid
- Ownership information (owner must be the company)

The combination of size type and class produces four supported vehicle categories:
- Economy Small: 4-seat sedan
- Economy Medium: 7-seat MPV / SUV
- Luxury Small: 4-seat luxury sedan
- Luxury Medium: Luxury MPV

Upon successful registration, the vehicle is assigned the **Incoming** lifecycle status.

**Acceptance Criteria:**

- **Given** a fleet manager is on the vehicle registration form,  
  **When** they submit the form with all required fields filled,  
  **Then** the vehicle is saved in the system with status **Incoming**, and a confirmation is displayed.

- **Given** a fleet manager attempts to submit the registration form,  
  **When** any required field is missing,  
  **Then** the system prevents submission and highlights the missing fields.

- **Given** a fleet manager enters a VIN or license plate number,  
  **When** that VIN or license plate already exists in the system,  
  **Then** the system rejects the entry and displays a duplicate error.

---

### FR-2: Vehicle Lifecycle Management

**Title:** Manage Vehicle Lifecycle Status

**Statement:** **As a** fleet manager, **I want** to update the lifecycle status of a vehicle, **so that** the system always reflects the vehicle's current operational state.

**Requirement Detail:**

A vehicle must progress through the following lifecycle statuses:
- **Incoming:** Vehicle acquired but not yet ready for rental.
- **Active:** Vehicle available for assignment to reservations.
- **Maintenance:** Vehicle undergoing service; not available for rental.
- **Decommissioning:** Vehicle being phased out; not available for new reservations.
- **Sold:** Vehicle removed from fleet.

Status transitions are managed manually by a fleet manager. The availability of a vehicle for reservations is governed by its status; only **Active** vehicles may be assigned.

**Acceptance Criteria:**

- **Given** a fleet manager views a vehicle record,  
  **When** they update its status to any of the defined lifecycle values,  
  **Then** the system saves the new status and records the change with a timestamp.

- **Given** a vehicle has a status other than **Active**,  
  **When** the reservation allocation system attempts to assign it,  
  **Then** the system excludes it from allocation.

- **Given** a vehicle is set to **Sold**,  
  **When** a fleet manager attempts to update its status again,  
  **Then** the system prevents the change, as **Sold** is a terminal state.

---

### FR-3: Insurance Expiry Block

**Title:** Block Vehicle Availability When Insurance Is About to Expire

**Statement:** **As a** fleet manager, **I want** the system to automatically block a vehicle from being assigned to reservations when its insurance is expiring within one week, **so that** the company does not expose itself to uninsured rental liability.

**Requirement Detail:**

The system must monitor the insurance expiry date of every active vehicle. When the insurance expiry date is within 7 calendar days from the current date, the vehicle must be flagged and excluded from reservation allocation. An alert must be sent to the fleet manager.

**Acceptance Criteria:**

- **Given** a vehicle's insurance expiry date is 7 or fewer days away,  
  **When** the system performs its daily check,  
  **Then** the vehicle is flagged as unavailable for new reservations and an alert is sent via email, SMS, and the dashboard.

- **Given** a vehicle has been flagged due to expiring insurance,  
  **When** the fleet manager updates the insurance record with a new valid expiry date beyond 7 days,  
  **Then** the block is automatically lifted and the vehicle becomes available again.

- **Given** a vehicle's insurance has already expired,  
  **When** a user views its record,  
  **Then** the system prominently displays the expired insurance warning.

---

### FR-4: Vehicle Retirement

**Title:** Manually Retire or Dispose of a Vehicle

**Statement:** **As a** fleet manager, **I want** to manually trigger the retirement or disposal of a vehicle, **so that** decommissioned assets are properly recorded and removed from active fleet operations.

**Requirement Detail:**

Retirement is a two-step process: first the vehicle is set to **Decommissioning**, then when formally disposed it is set to **Sold**. Both transitions are manual actions by the fleet manager. No automated rule triggers retirement.

**Acceptance Criteria:**

- **Given** a fleet manager selects a vehicle to decommission,  
  **When** they set its status to **Decommissioning**,  
  **Then** the vehicle is no longer available for new reservations and the status change is timestamped.

- **Given** a vehicle is in **Decommissioning** status,  
  **When** the fleet manager marks it as **Sold**,  
  **Then** the vehicle record is archived with final disposal details.

- **Given** a vehicle has active or upcoming reservations,  
  **When** a fleet manager attempts to set it to **Decommissioning**,  
  **Then** the system displays a warning listing the conflicting reservations before allowing the action.

---

### FR-5: Home Location Assignment

**Title:** Assign and View a Vehicle's Home Location

**Statement:** **As a** fleet manager, **I want** each vehicle to be assigned to a home location, **so that** inventory is tracked per location and vehicles can be dispatched from the correct base.

**Requirement Detail:**

Every vehicle must have exactly one home location at any given time. The home location is set during vehicle onboarding and can be changed through a transfer process (see FR-6). The system must display the current home location on the vehicle record.

**Acceptance Criteria:**

- **Given** a fleet manager is registering a new vehicle,  
  **When** they complete onboarding,  
  **Then** a home location must be selected and saved with the vehicle record.

- **Given** a vehicle record is viewed,  
  **When** a fleet manager opens the location section,  
  **Then** the current home location is displayed clearly.

- **Given** there are multiple vehicles,  
  **When** a fleet manager views inventory per location,  
  **Then** only vehicles with that home location are listed.

---

### FR-6: Location Transfer

**Title:** Transfer a Vehicle to a New Home Location

**Statement:** **As a** fleet manager, **I want** to transfer a vehicle to a different home location and have the transfer cost recorded, **so that** cross-location movements are tracked and costs are properly attributed.

**Requirement Detail:**

A transfer changes the vehicle's home location. Each transfer event is logged in the vehicle's home location history with:
- Previous home location
- New home location
- Transfer date
- Transfer cost (charged to the company pool)

**Acceptance Criteria:**

- **Given** a fleet manager initiates a location transfer for a vehicle,  
  **When** they specify the new home location, transfer date, and cost,  
  **Then** the home location is updated and a transfer record is added to the vehicle's history.

- **Given** a vehicle has been transferred,  
  **When** a fleet manager views the vehicle's location history,  
  **Then** all past home locations are listed in chronological order with associated dates and costs.

- **Given** a transfer cost is entered,  
  **When** the transfer is saved,  
  **Then** the cost is logged as a company pool charge.

---

### FR-7: Real-Time Vehicle Availability

**Title:** View Real-Time Vehicle Availability

**Statement:** **As an** operations manager, **I want** to view which vehicles are available in real time (from 2 hours after the current time), **so that** I can make accurate same-day dispatch and allocation decisions.

**Requirement Detail:**

Real-time availability covers the window from **current time + 2 hours** onward (within the same day). A vehicle is considered available in real time if:
- Its lifecycle status is **Active**.
- It has no active reservation, maintenance block, or insurance block.
- It is at its home location.

**Acceptance Criteria:**

- **Given** an operations manager opens the availability view,  
  **When** they select real-time mode,  
  **Then** the system displays all vehicles available from the current time + 2 hours.

- **Given** a vehicle has a reservation starting within 2 hours,  
  **When** the real-time view is refreshed,  
  **Then** that vehicle no longer appears as available.

- **Given** a vehicle's status is not **Active**,  
  **When** the real-time availability view is displayed,  
  **Then** that vehicle is excluded from the available list.

---

### FR-8: Planned Vehicle Availability

**Title:** View Planned Vehicle Availability

**Statement:** **As an** operations manager, **I want** to view planned vehicle availability for the next 1 to 30 days (H+1 to H+30), **so that** I can support future reservation planning and maintenance scheduling.

**Requirement Detail:**

Planned availability covers the window from **current time + 1 day** to **current time + 30 days**. The system accounts for existing reservations, confirmed maintenance schedules, and the 1-day post-return turnaround when computing planned availability.

**Acceptance Criteria:**

- **Given** an operations manager opens the availability view,  
  **When** they select a date range within H+1 to H+30,  
  **Then** the system displays vehicles available for that range.

- **Given** a vehicle has a confirmed maintenance block on a future date,  
  **When** planned availability is computed,  
  **Then** that vehicle is shown as unavailable on the blocked date.

- **Given** a vehicle has a reservation with a return date on day X,  
  **When** planned availability is computed,  
  **Then** the vehicle is shown as unavailable on day X + 1 (turnaround day) and available from day X + 2.

---

### FR-9: GPS Tracking

**Title:** Track Vehicle Location via GPS

**Statement:** **As an** operations manager, **I want** to view the live GPS location of every active vehicle on a map, **so that** I have continuous operational visibility over the fleet.

**Requirement Detail:**

The system must display the current GPS coordinates of each vehicle on an operational dashboard map. GPS data must be updated at a maximum interval of 5 minutes. Real-time updates are preferred where the telematics integration allows.

**Acceptance Criteria:**

- **Given** a vehicle has an active GPS device,  
  **When** an operations manager opens the fleet map,  
  **Then** each vehicle's location is plotted with its last known position and timestamp.

- **Given** a GPS update arrives,  
  **When** more than 5 minutes have elapsed since the last update,  
  **Then** the system flags the vehicle as having a stale location and alerts the operations team.

- **Given** an operations manager selects a vehicle on the map,  
  **When** the vehicle detail panel opens,  
  **Then** it shows the vehicle's current coordinates, last update time, and current lifecycle status.

---

### FR-10: Geofencing Alert

**Title:** Alert When a Vehicle Leaves Its Permitted Area

**Statement:** **As an** operations manager, **I want** to receive an alert when a vehicle exits its defined geofenced area, **so that** unauthorized movement or theft can be identified and acted upon promptly.

**Requirement Detail:**

Each vehicle may have a geofenced boundary defined. When the vehicle's GPS position is detected outside that boundary, the system must:
- Immediately generate an alert.
- Deliver the alert via email, SMS, and the operations dashboard.

**Acceptance Criteria:**

- **Given** a vehicle is assigned a geofence boundary,  
  **When** its GPS position moves outside the defined boundary,  
  **Then** the system generates an alert and delivers it via email, SMS, and the dashboard within 5 minutes.

- **Given** a geofencing alert has been generated,  
  **When** an operations manager views the alert,  
  **Then** it shows the vehicle ID, timestamp, last known location, and the boundary it exited.

- **Given** a vehicle returns inside its geofence,  
  **When** the next GPS update confirms its position is within the boundary,  
  **Then** the alert is automatically resolved and the manager is notified.

---

### FR-11: Automatic Reservation Allocation

**Title:** Automatically Allocate a Vehicle to a Reservation

**Statement:** **As a** reservation agent, **I want** the system to automatically assign an available vehicle to a new reservation, **so that** manual selection is not required for standard bookings.

**Requirement Detail:**

When a reservation is created, the system automatically selects a vehicle based on:
- The requested vehicle type (size + class) matching an available vehicle.
- First-come, first-served order among available matching vehicles.

No overbooking is permitted. If no matching vehicle is available for the requested period, the reservation cannot be confirmed.

**Acceptance Criteria:**

- **Given** a reservation is submitted for a specific vehicle type and date range,  
  **When** a matching available vehicle exists,  
  **Then** the system automatically assigns the vehicle and confirms the reservation.

- **Given** multiple matching vehicles are available,  
  **When** the system allocates,  
  **Then** it selects the vehicle whose availability starts earliest (first-come, first-served).

- **Given** no matching vehicle is available for the requested type and period,  
  **When** the reservation is submitted,  
  **Then** the system rejects it and informs the agent that no vehicle is available.

---

### FR-12: Vehicle Substitution

**Title:** Substitute an Unavailable Vehicle

**Statement:** **As an** operations manager, **I want** the system to offer a substitution when the originally allocated vehicle becomes unavailable, **so that** customer reservations are disrupted as little as possible.

**Requirement Detail:**

When the allocated vehicle can no longer fulfill a reservation, substitution follows this priority order:
1. Replace with another vehicle of the **same type** (same size + same class).
2. Replace with a **better vehicle** (upgrade in class or size).
3. If no substitution is possible, **refund** the customer.

**Acceptance Criteria:**

- **Given** an allocated vehicle becomes unavailable before the rental start,  
  **When** the system evaluates substitutions,  
  **Then** it first searches for a same-type vehicle; if found, it assigns it automatically.

- **Given** no same-type vehicle is available,  
  **When** the system evaluates substitutions,  
  **Then** it searches for a higher-class or larger vehicle; if found, it assigns it automatically.

- **Given** no same-type or better vehicle is available,  
  **When** the substitution check is complete,  
  **Then** the system flags the reservation for a full refund and notifies the operations manager.

---

### FR-13: Delivery & Pickup Scheduling

**Title:** Schedule a Vehicle Delivery or Pickup

**Statement:** **As a** customer, **I want** to schedule a vehicle delivery to my location or arrange a pickup, **so that** I can receive or return the vehicle without going to the rental office.

**Requirement Detail:**

- Delivery (drop-off to customer's location) and pickup (return collection from customer's location) are both offered as optional paid services.
- Scheduling is available every day between **06:00 and 19:00 local time**.
- Route planning is performed manually by the operations team.
- An additional service charge applies to each delivery or pickup.

**Acceptance Criteria:**

- **Given** a customer requests a delivery or pickup service,  
  **When** they select a date and time,  
  **Then** only time slots between 06:00 and 19:00 local time are available for selection.

- **Given** a delivery or pickup is scheduled,  
  **When** the booking is confirmed,  
  **Then** an additional service charge is added to the rental invoice.

- **Given** a time slot outside 06:00–19:00 is requested,  
  **When** the customer attempts to confirm,  
  **Then** the system rejects the request and displays the allowed time range.

---

### FR-14: Proof of Handover Capture

**Title:** Capture Proof of Vehicle Handover

**Statement:** **As a** delivery crew member, **I want** to capture proof of vehicle handover digitally, **so that** there is a verifiable, time-stamped record of the condition and delivery of the vehicle.

**Requirement Detail:**

At the time of vehicle handover (both delivery to customer and return from customer), the crew must capture:
- Photos of the vehicle (all sides).
- An e-form with customer signature, automatically recording the timestamp and geolocation of the signing event.

**Acceptance Criteria:**

- **Given** a crew member completes a vehicle handover,  
  **When** they submit the handover form,  
  **Then** the system requires at least one photo and a captured digital signature before the form can be submitted.

- **Given** the handover form is submitted,  
  **When** the record is saved,  
  **Then** the timestamp and GPS coordinates of the signing event are automatically recorded and stored.

- **Given** a handover record is stored,  
  **When** a fleet manager or operations manager views it,  
  **Then** they can see all photos, the signature, and the captured timestamp and geolocation.

---

### FR-15: Customer Identity Verification at Pickup

**Title:** Verify Customer Identity at Vehicle Pickup

**Statement:** **As a** delivery crew member, **I want** to verify the customer's identity before handing over the vehicle, **so that** only the authorized renter receives the vehicle.

**Requirement Detail:**

Identity verification at pickup must include:
- A scan or photo of the customer's driver's license.
- A live selfie compared against the national identifier (national ID or passport) on file for the customer.

Both checks must be completed and recorded before the handover is finalized.

**Acceptance Criteria:**

- **Given** a crew member is performing a vehicle handover,  
  **When** they open the verification step,  
  **Then** the system prompts them to capture a driver's license scan and a selfie.

- **Given** both the driver's license and selfie are captured,  
  **When** the crew member confirms the match,  
  **Then** the verification is recorded and the handover can proceed.

- **Given** the crew member skips the identity verification step,  
  **When** they attempt to finalize the handover,  
  **Then** the system prevents completion and displays a verification required message.

---

### FR-16: Return Inspection

**Title:** Perform a Standardized Vehicle Return Inspection

**Statement:** **As an** inspection crew member, **I want** to complete a structured return inspection checklist, **so that** the vehicle's condition at return is formally documented and any chargeable damage is identified.

**Requirement Detail:**

The return inspection includes the following steps, all of which are mandatory:
1. **Photos:** Vehicle photos from all sides.
2. **Damage checklist:** Structured review covering:
   - Exterior (body panels, glass, wheels)
   - Interior (seats, dashboard, trim)
   - Engine check
   - Electrical instruments check
3. **Fuel level recording** (see FR-18).

**Chargeable vs. Normal Wear:**
- **Chargeable damage:** Damage caused by accident that is visibly apparent — for example, a body dent or torn leather seat. Must be visible to the naked eye.
- **Normal wear:** Engine and electrical instruments issues are considered normal wear and are not chargeable.

**Acceptance Criteria:**

- **Given** an inspection crew member opens a return inspection form,  
  **When** they complete all sections (photos, damage checklist, fuel level),  
  **Then** the system saves the completed inspection linked to the specific vehicle and rental record.

- **Given** a damage item is marked as present on the checklist,  
  **When** it is classified as chargeable (visible exterior or interior accident damage),  
  **Then** the system flags it for damage charge processing.

- **Given** engine or electrical instrument issues are noted,  
  **When** the inspection is submitted,  
  **Then** those items are recorded as normal wear and no charge is applied.

- **Given** the inspection crew attempts to submit the form with missing sections,  
  **When** any mandatory section is incomplete,  
  **Then** the system prevents submission and highlights the missing sections.

---

### FR-17: Damage Recording with Positional Mapping

**Title:** Record Vehicle Damage with Positional Mapping on a Vehicle Diagram

**Statement:** **As an** inspection crew member, **I want** to mark damage locations on a vehicle diagram and attach supporting photos, **so that** the exact location and nature of each damage item is accurately documented.

**Requirement Detail:**

The damage recording interface must include:
- An interactive vehicle diagram (top view and side views).
- The ability to pin a damage marker on the diagram at the exact location of the damage.
- The ability to attach one or more photos to each damage marker.

**Acceptance Criteria:**

- **Given** an inspection crew member is recording damage,  
  **When** they tap or click a location on the vehicle diagram,  
  **Then** a damage marker is placed at that position and a photo upload prompt is shown.

- **Given** a damage marker is placed,  
  **When** at least one photo is attached and the damage is saved,  
  **Then** the marker, photo(s), and a description are linked to the vehicle's inspection record.

- **Given** a damage record is viewed by a fleet manager,  
  **When** they open the inspection record,  
  **Then** they see the vehicle diagram with all damage markers and can click each to view its photos and description.

---

### FR-18: Fuel Level Check on Return

**Title:** Record and Charge for Fuel Shortfall on Return

**Statement:** **As an** inspection crew member, **I want** to record the vehicle's fuel level at return, **so that** the customer can be charged if the fuel level is lower than at the time of delivery.

**Requirement Detail:**

The company operates a same-level fuel policy: the vehicle must be returned with the same fuel level as when it was delivered. The delivery fuel level is recorded at handover (FR-14). If the return fuel level is lower, a fuel shortfall charge is applied to the customer.

**Acceptance Criteria:**

- **Given** an inspection crew member records the return fuel level,  
  **When** the return fuel level is lower than the delivery fuel level,  
  **Then** the system calculates a fuel shortfall charge and adds it to the final invoice.

- **Given** the return fuel level equals or exceeds the delivery fuel level,  
  **When** the inspection is submitted,  
  **Then** no fuel charge is applied.

- **Given** the fuel shortfall charge is calculated,  
  **When** the inspection crew member views the return summary,  
  **Then** the charge amount is displayed before finalizing.

---

### FR-19: Maintenance Scheduling

**Title:** Schedule Mileage-Based Maintenance

**Statement:** **As a** fleet manager, **I want** the system to schedule maintenance for each vehicle at every 10,000 km milestone, **so that** vehicles are serviced regularly and fleet reliability is maintained.

**Requirement Detail:**

Maintenance milestones are set at every 10,000 km multiple of the vehicle's total odometer reading (10,000 km, 20,000 km, 30,000 km, etc.). The crew is expected to schedule and perform maintenance within a service window of 10,000 to 12,000 km from the previous service — meaning service is due at the 10,000 km milestone but must be completed no later than 12,000 km after the last service. The system calculates the upcoming milestone based on the current odometer reading and prompts scheduling when the vehicle is within 2,000 km of the next milestone.

**Acceptance Criteria:**

- **Given** a vehicle's odometer reading is updated,  
  **When** it is approaching the next 10,000 km multiple (within 2,000 km),  
  **Then** the system prompts the fleet manager to schedule the upcoming maintenance.

- **Given** a maintenance schedule is created for a vehicle on a specific date,  
  **When** the schedule is saved,  
  **Then** the vehicle is marked as unavailable for rental on that date.

- **Given** the fleet manager views a vehicle record,  
  **When** they open the maintenance section,  
  **Then** they see the full maintenance history and the next scheduled date.

---

### FR-20: Maintenance Availability Block

**Title:** Automatically Block Vehicle Availability During Maintenance

**Statement:** **As a** fleet manager, **I want** scheduled maintenance dates to automatically block the vehicle from being assigned to reservations, **so that** vehicles in service are never double-booked.

**Requirement Detail:**

When a maintenance event is scheduled for a vehicle on a given day, the vehicle must not appear as available for that day in either real-time or planned availability views. Maintenance takes priority over reservations. If a reservation exists that conflicts with a newly scheduled maintenance day, the operations manager must be alerted.

**Acceptance Criteria:**

- **Given** a maintenance event is scheduled for a vehicle on day X,  
  **When** the availability system is queried for day X,  
  **Then** the vehicle is excluded from available vehicles.

- **Given** a reservation exists for a vehicle and a maintenance event is scheduled for an overlapping date,  
  **When** the maintenance is saved,  
  **Then** the system alerts the operations manager of the conflict.

- **Given** a maintenance event is completed and the vehicle is set back to **Active**,  
  **When** the availability system is queried for dates after the maintenance,  
  **Then** the vehicle is again available for assignment.

---

### FR-21: Damage Incident Reporting

**Title:** Report a Damage Incident After Return Inspection

**Statement:** **As an** inspection crew member, **I want** to file a damage incident report for a returned vehicle, **so that** chargeable damage is formally logged and the vehicle is flagged for repair.

**Requirement Detail:**

During or after the return inspection, if chargeable damage is identified, the crew member submits a damage incident report through a web form. The report must include:
- Vehicle identifier
- Damage description per damaged area
- Photos linked to positional markers (from FR-17)
- Estimated repair cost from the internal price list

Upon report submission, the vehicle is automatically set to **Maintenance** status (not available for new rentals).

**Acceptance Criteria:**

- **Given** an inspection crew member identifies chargeable damage during a return inspection,  
  **When** they submit the damage incident report with all required fields,  
  **Then** the report is saved and the vehicle status is automatically changed to **Maintenance**.

- **Given** a damage incident report is filed,  
  **When** a fleet manager views the vehicle record,  
  **Then** the incident report with photos, damage locations, and cost estimate is visible.

- **Given** the crew member submits the form without attaching photos,  
  **When** chargeable damage items are present,  
  **Then** the system prevents submission and requires at least one photo per damage item.

---

### FR-22: Repair Cost Estimation

**Title:** Estimate Repair Costs Using Internal Price List

**Statement:** **As a** fleet manager, **I want** to estimate repair costs for a damaged vehicle using an internal price list, **so that** damage charges are consistently calculated and billed to the customer.

**Requirement Detail:**

The system must maintain an internal price list for common damage types (e.g., body dent repair, seat replacement). During damage incident reporting (FR-21), the inspection crew selects the damage type and the system retrieves the associated cost from the price list.

**Acceptance Criteria:**

- **Given** a damage type is selected during incident reporting,  
  **When** the form is populated,  
  **Then** the system pre-fills the estimated repair cost from the internal price list.

- **Given** the internal price list is updated by a fleet manager,  
  **When** new repair cost estimates are submitted,  
  **Then** the updated prices are applied to all future damage reports.

- **Given** a damage incident is finalized,  
  **When** the customer invoice is generated,  
  **Then** the repair cost is included as a line item in the final bill.

---

### FR-23: Fuel Charge Calculation

**Title:** Calculate and Apply Fuel Shortfall Charge

**Statement:** **As an** operations agent, **I want** the system to calculate the fuel shortfall charge when a vehicle is returned with less fuel than at delivery, **so that** customers are correctly billed for fuel consumed beyond their obligation.

**Requirement Detail:**

The fuel level at delivery is recorded as part of the proof of handover (FR-14). The fuel level at return is recorded during the return inspection (FR-18). If the return level is lower, the system computes the charge based on the fuel difference and a fuel price rate.

**Acceptance Criteria:**

- **Given** the return fuel level is lower than the delivery fuel level,  
  **When** the return inspection is completed,  
  **Then** the system calculates the fuel shortfall charge and adds it to the customer's invoice.

- **Given** the fuel levels at delivery and return are equal,  
  **When** the return inspection is completed,  
  **Then** no fuel charge is added.

- **Given** a fuel charge is applied,  
  **When** the customer's final invoice is generated,  
  **Then** the fuel shortfall charge appears as a separate line item with the quantity and rate.

---

### FR-24: Utilization Dashboard

**Title:** View Fleet Utilization Metrics

**Statement:** **As a** fleet manager, **I want** to view vehicle utilization metrics on a per-location and global basis, **so that** I can assess fleet performance and make informed decisions about fleet size and composition.

**Requirement Detail:**

The utilization dashboard must display, on a daily basis:
- Days each vehicle was actively rented (utilization days).
- Days each vehicle was idle.
- Per-location summary (total vehicles, utilization rate, idle count).
- Global summary across all locations.

**Acceptance Criteria:**

- **Given** a fleet manager opens the utilization dashboard,  
  **When** they select a date or date range,  
  **Then** the system displays each vehicle's utilization and idle days for that period, grouped by location.

- **Given** a fleet manager selects a specific location,  
  **When** the dashboard filters to that location,  
  **Then** only vehicles with that home location are shown in the metrics.

- **Given** a fleet manager selects the global view,  
  **When** the dashboard aggregates all locations,  
  **Then** a combined utilization rate and idle count are displayed.

---

### FR-25: Consecutive Idle Alert

**Title:** Alert for Vehicles Idle for 5 Consecutive Days

**Statement:** **As a** fleet manager, **I want** to be alerted when a vehicle has been idle for 5 or more consecutive days, **so that** I can investigate and take action to improve fleet utilization.

**Requirement Detail:**

The system must monitor each vehicle's rental activity. If a vehicle has not been rented for 5 consecutive days, an alert must be sent to the fleet manager via email, SMS, and the dashboard.

**Acceptance Criteria:**

- **Given** a vehicle has not been rented for 5 consecutive days,  
  **When** the end of the 5th idle day is reached,  
  **Then** the system sends an alert to the fleet manager via email, SMS, and the dashboard.

- **Given** a vehicle receives a new reservation after having been idle,  
  **When** the rental begins,  
  **Then** the consecutive idle counter resets.

- **Given** an idle alert has been sent,  
  **When** the fleet manager views the alert,  
  **Then** it shows the vehicle ID, current location, and the number of consecutive idle days.

---

### FR-26: Rental Extension

**Title:** Extend an Active Rental

**Statement:** **As a** customer, **I want** to extend my rental period, **so that** I can keep the vehicle for additional days without interruption.

**Requirement Detail:**

Extension requests are auto-approved if the vehicle is not already reserved for the extension period. The customer is charged the base daily rental rate plus a daily late charge for each extended day. If the extension period conflicts with an existing upcoming reservation for that vehicle, the system must warn the operations manager.

**Acceptance Criteria:**

- **Given** a customer requests a rental extension,  
  **When** the vehicle has no conflicting reservation for the extension period,  
  **Then** the extension is auto-approved and the customer is charged the base rate plus the daily late charge.

- **Given** the extension period overlaps with an existing reservation for the same vehicle,  
  **When** the extension request is evaluated,  
  **Then** the system alerts the operations manager of the conflict.

- **Given** the extension is approved,  
  **When** the new return date is confirmed,  
  **Then** the rental record is updated with the new end date and additional charges are added to the invoice.

---

### FR-27: Early Return & Repositioning

**Title:** Process an Early Vehicle Return and Reposition for New Rental

**Statement:** **As an** operations manager, **I want** an early-returned vehicle to be automatically made available for new customers after the standard turnaround, **so that** fleet utilization is maximized.

**Requirement Detail:**

When a customer returns a vehicle before the scheduled return date, the vehicle enters the standard 1-day turnaround cleaning and preparation process. After the turnaround day, the vehicle is repositioned and made available for new reservations. No credit is automatically issued for unused rental days (this is a billing concern handled elsewhere).

**Acceptance Criteria:**

- **Given** a customer returns a vehicle early,  
  **When** the return is processed,  
  **Then** the vehicle's availability is opened starting from the return date + 1 day (after turnaround).

- **Given** an early return is recorded,  
  **When** the system updates availability,  
  **Then** the vehicle appears in planned availability from the day after the turnaround.

- **Given** an early-returned vehicle completes its turnaround,  
  **When** the turnaround period ends,  
  **Then** the vehicle status returns to **Active** and is available for new allocations.

---

### FR-28: Lost Key Charge

**Title:** Charge Customer for a Lost Vehicle Key

**Statement:** **As an** operations agent, **I want** to record a lost key incident and apply the associated charge to the customer's invoice, **so that** the company recovers the cost of key replacement.

**Requirement Detail:**

If a customer loses a vehicle key, the operations agent records the lost key incident and applies a charge based on the internal price list. Physical key replacement is handled manually outside the system.

**Acceptance Criteria:**

- **Given** a customer reports or is found to have lost a vehicle key,  
  **When** an operations agent records the lost key incident,  
  **Then** the associated charge is added to the customer's invoice.

- **Given** a lost key charge is applied,  
  **When** the final invoice is generated,  
  **Then** the lost key charge appears as a separate line item.

- **Given** the lost key incident is recorded,  
  **When** the vehicle record is viewed,  
  **Then** the incident is logged in the vehicle's event history.

---

### FR-29: Workforce Task Assignment

**Title:** Assign Post-Return Tasks to Staff

**Statement:** **As a** supervisor, **I want** to manually assign cleaning and inspection tasks to staff after a vehicle is returned, **so that** every returned vehicle is prepared for the next rental in a timely manner.

**Requirement Detail:**

Task assignment rules:
- If a vehicle is returned **before 15:00 local time**, tasks (cleaning, inspection) are assigned to staff on the **same day**.
- If a vehicle is returned **at or after 15:00 local time**, tasks are assigned to staff on the **next business day**.

Task assignment is manual and performed by the supervisor. The 1-day turnaround (FR-7, FR-8) accounts for this preparation day.

**Acceptance Criteria:**

- **Given** a vehicle is returned before 15:00 local time,  
  **When** the supervisor opens the task assignment panel,  
  **Then** the task is listed under the current day's queue.

- **Given** a vehicle is returned at or after 15:00 local time,  
  **When** the supervisor opens the task assignment panel,  
  **Then** the task is listed under the next day's queue.

- **Given** a supervisor assigns a task to a staff member,  
  **When** the assignment is saved,  
  **Then** the staff member's name and assignment timestamp are recorded against the vehicle's turnaround record.

---

## Non-Functional Requirements

*(To be defined.)*

---

## Dependency & Constraints

- **Desktop web only:** All management and operational interfaces (fleet management, inspection forms, task assignment, dashboards) are designed for desktop web browsers. Mobile-optimized interfaces are not in scope for this phase, except for field crew handover and inspection forms which must be accessible on mobile web.
- **No third-party integrations in this phase:** No existing DMS, telematics provider, insurance adjuster portal, or external repair shop integrations are in scope. GPS device connectivity specifics and telematics provider selection are to be determined.
- **No automated repositioning or AI damage detection:** All operational decisions (route planning, task assignment, vehicle retirement) are manual.
- **No peer-to-peer or subscription models:** Only direct company-owned fleet rentals are supported in this phase.
- **No region-specific compliance requirements** in this phase (emissions, license categories, EV subsidies).
- **No after-hours key drop:** Pickup and drop-off services are strictly within 06:00–19:00 local time. No after-hours operations are supported.
- **Fleet size is variable:** Initial fleet will consist of economy vehicles; fleet size is determined by business decisions and may grow over time.

---

## Success Metrics

- **Zero double-booking incidents** resulting from vehicle allocation errors.
- **100% of returned vehicles** have a completed digital inspection record before being set back to active status.
- **Vehicle availability data accuracy** of ≥ 99% (no vehicles appearing as available when blocked or in maintenance).
- **Insurance expiry block compliance:** No vehicle with expired or expiring insurance (within 7 days) is assigned to a reservation.
- **Maintenance schedule adherence:** ≥ 95% of vehicles serviced within the 10,000–12,000 km service window.
- **Geofencing alert delivery time** ≤ 5 minutes from boundary breach detection.
- **GPS location staleness rate** < 5% of active vehicle-hours (location updated within 5-minute threshold).
- **Idle vehicle identification:** 100% of vehicles idle for 5 consecutive days trigger an alert.
