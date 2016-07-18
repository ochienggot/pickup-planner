# Pickup-planner
The goal of the Pickup planner system is to provide a pickup service that can be used by fleet managment companies as a travel service for city residents, satisfying their varying travel needs. The front end consists of the Client app and Vehicle app. The Client app allows users to register for the service and issue travel requests. The system backend processes these requests and sends a travel trajectory to the Vehicle app, which visualizes and updates the trips the vehicle should take in order to satisfy the travel requests.

The implementation of the Pickup planner service partially builds on components from the Citypulse framework, i.e. Decision Support and Geospatial Data Infrastructure (GDI). It includes a scheduling and dynamic route optimization algorithm. The system currently uses OpenStreetMap data for Stockholm, but can easily incorporate data for other cities through the GDI.


# Dependencies

Database e.g. PostgreSQL/psycopg2  
Clustering library e.g. Scikit-Learn  
Networkx  
ASP solver (Clingo4)

# Installation
Client and vehicle app: installed on Android devices.  
Trip Generator and Traffic Handler: run on a backend server by a cron job.
