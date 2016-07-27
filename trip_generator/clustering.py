import pprint
import math
import random
import ast

import psycopg2 as pg
import numpy as np
from sklearn.cluster import KMeans
import matplotlib.pyplot as plt
from scipy.cluster.vq import kmeans2, whiten

# TODO: change from Google API to GDI

def cluster_requests(group_requests):
    ''' Clusters requests from the database, cluster
    size based on the vehicle capacity
    '''
    conn = pg.connect(database="ngot", host = "127.0.0.1", port = "5432")
    cursor = conn.cursor()
    cursor.execute("SELECT latitude, longitude, node_id FROM nodes WHERE node_id != 1 AND node_id != 100") # TODO: Handle origin and end nodes better
    rows = cursor.fetchall()
    coords_id = {}
    for row in rows:
        coords_id[row[0], row[1]] = (row[2])

    coordinates = np.array([[row[0], row[1]] for row in rows])
    num_passengers = len(rows)
    vehicle_capacity = 4.0
    if num_passengers > vehicle_capacity:
        num_clusters = math.ceil(num_passengers/vehicle_capacity)
        #print "With clustering, num travelers = " + str(num_passengers)
        #print "Num clusters = " + str(num_clusters) 
    else:
        num_clusters = 1

    # Random assignment/no clustering
    if group_requests: # TODO: more refactoring
        random_group(coords_id, num_passengers, num_clusters)
        return

    num_clusters += 4 # Deciding cluster size
    km = KMeans(init='k-means++', max_iter=10000, n_init=1, verbose=0, n_clusters=int(num_clusters))
    clusters = km.fit_predict(coordinates)

    result = {}
    if num_clusters == 1:
        cluster_0 = np.where(clusters==0)
        coords_0 = coordinates[cluster_0]
        for loc in coords_0.tolist():
            result[coords_id[(loc[0], loc[1])]] = (loc[0], loc[1])
    else:
        clusters_all = {}
        for i in range(int(num_clusters)):
            result_i = {}
            cluster_i = np.where(clusters==i)
            coords_i = coordinates[cluster_i]
            for loc_i in coords_i.tolist():
                result_i[coords_id[(loc_i[0], loc_i[1])]] = (loc_i[0], loc_i[1])

            clusters_all[i] = result_i
        print "All clusters..............." 
        pprint.pprint(clusters_all)

        f = open('cluster_results.txt', 'w')
        f.write(str(clusters_all))

    f.close()
    cursor.close()
    conn.close()
    #print "Passengers in this cluster = " + str(len(result))
    return result

def cluster_requests_7():
    ''' Clusters requests from the database, cluster
    size based on the vehicle capacity
    '''
    f = open('cluster_7.txt', 'r')
    nodes = ast.literal_eval(f.read())
    node_ids = [val for val in nodes[1]]
    f.close()

    conn = pg.connect(database="ngot", host = "127.0.0.1", port = "5432")
    cursor = conn.cursor()
    cursor.execute("SELECT latitude, longitude, node_id FROM nodes WHERE node_id = ANY(%s);", (node_ids,))
    rows = cursor.fetchall()
    coords_id = {}
    for row in rows:
        coords_id[row[0], row[1]] = (row[2])

    coordinates = np.array([[row[0], row[1]] for row in rows])
    num_passengers = len(rows)
    vehicle_capacity = 4.0
    if num_passengers > vehicle_capacity:
        num_clusters = math.ceil(num_passengers/vehicle_capacity)
        #print "With clustering, num travelers = " + str(num_passengers)
        #print "Num clusters = " + str(num_clusters) 
    else:
        num_clusters = 1

    num_clusters += 1
    km = KMeans(init='k-means++', max_iter=10000, n_init=1, verbose=0, n_clusters=int(num_clusters))
    clusters = km.fit_predict(coordinates)

    result = {}
    if num_clusters == 1:
        cluster_0 = np.where(clusters==0)
        coords_0 = coordinates[cluster_0]
        for loc in coords_0.tolist():
            result[coords_id[(loc[0], loc[1])]] = (loc[0], loc[1])

    else:
        clusters_all = {}
        for i in range(int(num_clusters)):
            result_i = {}
            cluster_i = np.where(clusters==i)
            coords_i = coordinates[cluster_i]
            for loc_i in coords_i.tolist():
                result_i[coords_id[(loc_i[0], loc_i[1])]] = (loc_i[0], loc_i[1])

            clusters_all[i] = result_i
        #print "All clusters..............." 
        pprint.pprint(clusters_all)

        f = open('7_cluster_results.txt', 'w')
        f.write(str(clusters_all))

    f.close()
    cursor.close()
    conn.close()
    #print "Passengers in this cluster = " + str(len(result))
    return result

def random_group(coords_id, num_passengers, num_clusters):
    '''
    Randomly groups requests into groups that can be served by the given number of vehicles. Writes results to file

    '''
    all_groups = {}
    group_id = 0
    index = 0
    rand_groups = random.sample(range(num_passengers), num_passengers)
    all_lst = [node for node in coords_id] # index back to get node_id
    vehicle_load = 0
    vehicle_capacity = 4

    # TODO: better algorithm?
    while index < num_passengers and group_id < num_clusters:
        load_this_vehicle = 0
        this_group = {}
        while load_this_vehicle < vehicle_capacity:
            rand_index = rand_groups[index] # retrieve random index
            rand_node_coord = all_lst[rand_index]
            rand_node_id = coords_id[rand_node_coord]
            this_group[rand_node_id] = rand_node_coord
            # fill this vehicle
            load_this_vehicle += 1
            # Next traveler
            index += 1
            if index >= num_passengers:
                break
        # New vehicle
        all_groups[group_id] = this_group
        group_id += 1

    #print "Random clusters........"
    pprint.pprint(all_groups)
    f = open('group_results.txt', 'w')
    f.write(str(all_groups))
    f.close()

if __name__ == '__main__':
    cluster_requests(False)
