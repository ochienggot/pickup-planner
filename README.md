# Pickup-planner
The goal of the Pickup planner system is to provide a pickup service that can be used by fleet managment companies as a travel service for city residents destinations, satisfying their varying travel needs. The client app allows users to register to the system and send travel requests. The system backed processes the travel requests and presents a travel trajectory to the vehicle app, which is used by the driver to service the requests. 

The implementation of the Pickup planner service partially builds on components from the Citypulse framework. It
implements a scheduling and dynamic route optimization algorithm together with a working prototype. Currently, the
system only uses OpenStreetMap data for Stockholm, but can easily incorporate data for other cities.

# Installation
Client and vehicle app: installed on Android devices.
Trip Generator and Traffic Handler: run on a backend server

# Dependencies
DBMS e.g. PostgreSQL and psycopg2
ML library e.g. Scikit-Learn
ASP solver (Clingo4)
