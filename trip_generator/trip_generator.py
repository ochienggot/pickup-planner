#script(python)

import googlemaps
import json
import requests
import networkx as nx
import psycopg2 as pg
from datetime import datetime
import itertools
import ast

from clustering import cluster_requests
from CpGdiInterface import CpGdiInterface


''' Travel requests by users stored in the form user=>[source, destination, time], where the time field indicates the time constraints for each user
'''
class DBConnection():
    '''
        Class for connecting to the database
    '''

    def __init__(self):
        self.conn = pg.connect(database="ngot", host="127.0.0.1", port="5432")
    
    def fetch_rows(self, dest_id):
        self.cur = self.conn.cursor()
        self.cur.execute("SELECT DISTINCT ON (source) request_id, source, destination, destination_id FROM REQUESTS WHERE destination_id=%s", (dest_id,));
        self.rows = list(self.cur.fetchall())

    def get_rows(self):
        return self.rows

    def close_connection(self):
        self.cur.close()
        self.conn.close()

def get_requests_fromDB(dest_id):
    ''' Queries the database for travel requests, adds source/destination depot.
    @param dest_id Destination node ID, integer

    @return location in lat/lon format
    '''
    db = DBConnection()
    db.fetch_rows(dest_id)
    rows = db.get_rows()

    source_loc = {}
    source_distinct = [] #TODO: handle w/ PostgreSQL distinct
    dst_loc = {}

    for row in rows:
        lat, lon = row[1].split(",")
        source_lat = lat[1:]
        source_lon = lon[:-1]
        source_coords = (source_lat, source_lon)
        if source_coords not in source_distinct:
            source_distinct.append(source_coords)
            source_loc[row[0]] = source_coords

        # Destination
        d_lat, d_lon = row[2].split(",")
        dst_lat = d_lat[1:]
        dst_lon = d_lon[:-1]
        dst_coords = (dst_lat, dst_lon)
        dst_loc[row[3]] = dst_coords

    db.close_connection()
    return source_loc, dst_loc

def request_distance(str_origins, str_destinations):
    ''' Gets the distance between the origin and destination in metres. Queries Google Maps Distance Matrix API. To be replace with inhouse GDI.
    '''
    res = requests.get('https://maps.googleapis.com/maps/api/distancematrix/json?origins=' + str_origins
                       +'&destinations=' + str_destinations +
                       '&language=swKE&key=AIzaSyDY3kE9ShEiHTRzl1a7G-V7wgKeG1HpA0k')

    costs = {}
    json_str = json.loads(res.text)
    if res.status_code == requests.codes.ok:
        for index in range(len(json_str['origin_addresses'])):
            start =  json_str['origin_addresses'][index]
            end =  json_str['destination_addresses'][index]
            cost = json_str['rows'][index]['elements'][index]['duration']['value']
            costs[start + " " + end] = cost

    return json_str['rows'][index]['elements'][index]['distance']['value']

def get_cost_distance(origin, destination):
    """ Return the distance in metres from origin to destination obtained from GDI

    origin -- start latitude/longitude
    destination -- end latitude/longitude
    """
    gdi = CpGdiInterface()
    src = origin.split(',')
    dst = destination.split(',')
    COST_METRIC = "pollution"
    gdi.get_city_routes(src[1], src[0], dst[1], dst[0], COST_METRIC, 1)

    cost = gdi.get_route_cost()
    #return gdi.get_route_cost()
    # print "Cost of route = " + str(cost)
    if cost is None:
        return 0
    return cost

def build_graph(node_list):
    """ Build and return a weigted graph based on the source locations and adds node costs

    node_list -- the list of nodes and corresponding lat/lat 
    """
    start_depot = ['59.84228', '17.6443627'] # Polacks
    stop_depot = ['59.6497622', '17.9237807'] # Arlanda
    node_list[1] = start_depot
    node_list[100] = stop_depot

    G = nx.Graph()
    for node in itertools.combinations(node_list.keys(), 2):
        G.add_edge(*node)

    conn = pg.connect(database="ngot", host="127.0.0.1", port="5432")
    cur = conn.cursor()

    for n in G.nodes():
        WT_LAT = 'latitude'
        WT_LNG = 'longitude'
        G.node[n][WT_LAT] = node_list[n][0]
        G.node[n][WT_LNG] = node_list[n][1]
        # TODO: Add exception handling here
        insert_nodes(n, node_list[n][0], node_list[n][1], cur)

    for edge in G.edges():
        node1 = edge[0]
        node2 = edge[1]
        src_lat, src_lon = node_list[node1][0], node_list[node1][1]
        dst_lat, dst_lon = node_list[node2][0], node_list[node2][1]
        EDGE_WEIGHT = 'weight'
        G[node1][node2][EDGE_WEIGHT] = edge_cost([src_lat, src_lon], 
                [dst_lat, dst_lon])
        insert_edges(node1, node2, G[node1][node2][EDGE_WEIGHT], cur)

    conn.commit()
    cur.close()
    conn.close()
    return G

def insert_edges(node1, node2, weight, cursor):
    ''' Persist edges into database
    @param node1 node
    @param node2 node
    @weight distance between node1 and node2
    '''
    cursor.execute("INSERT INTO edges (node1, node2, weight) VALUES (%s, %s, %s)", (node1, node2, weight))

# TODO: factor insert_nodes to be done asynchronously outside ASP, once users register requests (build client UI and connect to DB)

def insert_nodes(node, lat, lon, cursor):
    ''' Insert node into database
    @param node: node
    @param lat: latitude
    @param lon: longitude
    '''
    cursor.execute("INSERT INTO nodes (node_id, latitude, longitude) VALUES (%s, %s, %s)", (node, lat, lon))

def edge_cost(src_loc, dst_loc):
    '''
    Calculate the cost of an edge on the graph
    Using Google Maps Matrix API for now, to change later
    '''
    source = str(src_loc[0]) + ',' + str(src_loc[1])
    destination = str(dst_loc[0])  + ',' + str(dst_loc[1])

    # Migrated from Google API to GDI
    #return  request_distance(source, destination) 
    return get_cost_distance(source, destination)

def destinations():
    ''' Get the destination IDs from the database
    '''
    conn = pg.connect(database="ngot", host="127.0.0.1", port="5432")
    cur = conn.cursor()
    cur.execute("SELECT DISTINCT destination_id FROM requests")
    dest = []
    for row in cur.fetchall()[0]:
        dest.append(row)
    conn.close()
    cur.close()

    return 100
    #return dest  # TODO: fix list of destinations

def nodes(dest_id):
    ''' Construct graph, group or cluster request and return resulting nodes

        dest_id -- integer uniquely identifier for each destination
    '''
    src, dst = get_requests_fromDB(dest_id) 
    G = build_graph(src) # TODO: Graph built one-time
    # Cluster nodes from built graph

    f = open('new_cluster_results.txt', 'r') # clusters from graph
    #f = open('new_group_results.txt', 'r') # random groups
    all_clusters = ast.literal_eval(f.read())
    f.close()
    cluster_id = 7
    cluster = [c for c in all_clusters[cluster_id].keys()]

    '''
    temp = {}
    temp[cluster_id] = cluster
    f2 = open('cluster_7.txt', 'w')
    f2.write(str(temp))
    f2.close()
    '''
    print "cluster " + str(cluster_id)  + " Size = " + str(len(cluster))

    cluster_nodes = [node_id for node_id in cluster]
    SOURCE_DEPOT_ID = 1
    cluster_nodes.append(SOURCE_DEPOT_ID) 
    destinations = [d for d in dst.keys()]
    cluster_nodes += destinations

    return cluster_nodes

def neighbors(node):
    '''
    Return the neighbors to a node
    @param node integer uniquely identifying a node
    '''
    conn = pg.connect(database="ngot", host="127.0.0.1", port="5432")
    cur = conn.cursor()
    cur.execute("SELECT node1, node2 FROM edges WHERE node1=%s OR node2=%s", (node, node))
    rows = list(cur.fetchall())
    neighbors = []

    for row in rows:
        if row[0] != node:
            neighbors.append(row[0])
        if row[1] != node:
            neighbors.append(row[1])
    cur.close()
    conn.close()

    return neighbors

def cost(node1, node2):
    ''' Calculate the cost between two nodes in the graph

    node1 -- unique integer for node1
    node2 -- unique integer for node2
    '''
    conn = pg.connect(database="ngot", host="127.0.0.1", port="5432")
    cur = conn.cursor()
    cur.execute("SELECT weight FROM edges WHERE (node1=%s AND node2=%s) OR (node2=%s AND node1=%s)", (node1, node2, node1, node2))
    rows = list(cur.fetchall())
    cost = rows[0][0]
    cur.close()
    conn.close()

    return int(cost)

#end.
