# Assignment #02 - Software Architecture and Platforms - a.y. 2024-2025

- **Description:** Consider the "EBike application" seen in the first assignment. The objective of the second assignment is to design and develop a distributed version based on microservices and microservices patterns - as discussed in modules 2.X and in Lab Notes about microservices patterns, and adopting DDD as reference method for the analysis and development stage.
    
- **Deliverable**:  a zipped folder ``Assignment-02-<Surname>`` including a maven-based or gradle-based project, with sources and the report in PDF. The deliverable can be submitted using a link on the course web site.

- **Deadline:** November 29, 2024 - 9:00 AM

## Requirements

### User Stories

|As a| I want to|so that I can|
|----|----------|-------------|
|user|go on a ride with a rented bike|leave it wherever i want|
|user|check my credit|understand if it needs to be recharged|
|user|recharge my credit|go on a ride|
|system administrator|see the current position of every bike|check if was left too far|
|system administrator|see which users are currently riding a bike|spot any anomaly if present|
|system administrator|see all the registered users and their credit|spot any anomaly if present|
|system administrator|add new bikes to the system|increase the number of bikes in the future|

### Use cases

![Use case diagram](./doc/diagrams/use-cases.png)

#### Scenarios

- Go on a ride:
    1. The user chooses an available bike and selects "Start ride"
    1. The user can see his credits updating while he's riding
    1. The user selects "End ride"

- Check credit:
    1. The user sees his credit right in the home screen

- Recharge credit:
    1. The user selects a "recharge credit" button
    1. The user inserts how much credits he wants to deposit
    1. The user confirms

<!-- TODO: where are things like login / credit deduction -->