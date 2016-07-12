# Pickup-planner
The goal of the Pickup planner system is to provide a pickup service that can be used by fleet managment companies as a travel service for city residents, satisfying their varying travel needs. The client app allows users to register and subsequently issue travel requests. The system backend processes these requests and sends a travel trajectory to the vehicle app, which presents and updates the trips the vehicle should take in order to satisfy the requests.

The implementation of the Pickup planner service partially builds on components from the Citypulse framework. This includes a scheduling and dynamic route optimization algorithm as well as a working prototype. The system currently uses OpenStreetMap data for Stockholm, but can easily incorporate data for other cities through Citypulse's Geospatial Data Infrastructure component.

# Installation
Client and vehicle app: installed on Android devices.
Trip Generator and Traffic Handler: run on a backend server

# Dependencies
DBMS e.g. PostgreSQL and psycopg2

ML library e.g. Scikit-Learn

ASP solver (Clingo4)
