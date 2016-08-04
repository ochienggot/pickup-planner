#!flask/bin/python
from flask import Flask, jsonify, abort
from flask import make_response
from flask import request, Response
from flask import url_for
from functools import wraps
import psycopg2 as pg


app = Flask(__name__)

def check_auth(username, passwd):
    """ Called to verify that a username/passwd combination is valid
    """
    return username == 'admin' and passwd == 'p@ssw0rd'

def authenticate():
    """ Sends a 401 response that enables Basic auth
    """
    return Response(
        'Unauthorized access', 401,
        {'WWW-Authenticate': 'Basic realm="Login Required"'})

def requires_auth(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        auth = request.authorization
        if not auth or not check_auth(auth.username, auth.password):
            return authenticate()
        return f(*args, **kwargs)
    return decorated

def make_public_request(request):
    """ Return a public version of request

    request -- record with request_id, source and destination columns
    """
    new_request = {}
    new_request['uri'] = url_for('get_requests', request_id=request[0], _external=True)
    new_request['source'] = request[1]
    new_request['destination'] = request[2]

    return new_request

@app.route('/clientapp/requests', methods=['GET'])
@requires_auth
def get_requests():
    """ Query and return requests from the database
    """
    conn = pg.connect(database="ngot", host="127.0.0.1", port="5432")
    cursor = conn.cursor()
    cursor.execute("SELECT request_id, source, destination FROM requests")
    rows = list(cursor.fetchall())
    cursor.close()
    conn.close()

    return jsonify({'requests': [make_public_request(req) for req in rows]})

@app.route('/clientapp/requests/<int:request_id>', methods=['GET'])
@requires_auth
def get_request(request_id):
    """ Query and return requests from the database
    """
    conn = pg.connect(database="ngot", host="127.0.0.1", port="5432")
    cursor = conn.cursor()
    cursor.execute("SELECT request_id, source, destination FROM requests WHERE request_id=%s", (request_id,))
    rows = list(cursor.fetchall())
    cursor.close()
    conn.close()

    return jsonify({'requests': [make_public_request(req) for req in rows]})

# Returns a list of vehicle trips in the DB
@app.route('/vehicleapp/vehicle_trips', methods=['GET'])
@requires_auth
def get_vehicle_trips():
    """ Return the pickup points for the generated vehicle trips
    """
    conn = pg.connect(database="ngot", host="127.0.0.1", port="5432")
    cursor = conn.cursor()
    # LESSON: This took me over 2 hours to fix
    pg.extensions.register_type(
        pg.extensions.new_array_type(
        (1017,), 'PICKUP_POINTS[]', pg.STRING))
    cursor.execute("SELECT pickup_points FROM vehicletrips")
    rows = cursor.fetchone()
    cursor.close()
    conn.close()

    return jsonify({'vehicle_trips': rows})

@app.route('/clientapp/requests', methods=['POST'])
@requires_auth
def create_request():
    """ Persist request in database
    """
    #if not request.json in request.json:
        #abort(404)

    conn = pg.connect(database="ngot", host="127.0.0.1", port="5432")
    cursor = conn.cursor()
    #request_id = request.json['request_id']
    source = request.json['source']
    destination = request.json['destination']
    cursor.execute("INSERT INTO requests (source, destination) VALUES (%s, %s)", (source, destination))
    rows = cursor.rowcount
    conn.commit()
    cursor.close()
    conn.close()

    return jsonify({'rows': rows}), 201

@app.route('/clientapp/requests/<int:request_id>', methods=['PUT'])
@requires_auth
def update_request(request_id):
    """ Update request with request_id
    """
    conn = pg.connect(database="ngot", host="127.0.0.1", port="5432")
    cursor = conn.cursor()
    if 'source' in request.json:
        source = request.json['source']
        cursor.execute("UPDATE requests SET source=%s WHERE request_id=%s", (source, request_id))

    if 'destination' in request.json:
        destination = request.json['destination']
        cursor.execute("UPDATE requests SET destination=%s WHERE request_id=%s", (destination, request_id))

    # allow change of time constraints
    if 'time_constraints' in request.json:
        travel_time = request.json['destination']
        cursor.execute("UPDATE requests SET travel_time=%s WHERE request_id=%s", (travel_time, request_id))

    dst_id = request.json['destination_id']
    if dst_id is not None:
        cursor.execute("UPDATE requests SET destination_id=%s WHERE request_id=%s", (dst_id, request_id))

    rows = cursor.rowcount
    conn.commit()
    cursor.close()
    conn.close()

    return get_request(request_id)

@app.route('/clientapp/requests/<int:request_id>', methods=['DELETE'])
@requires_auth
def delete_request(request_id):
    """ Delete request with request_id from the database, if present

    request_id -- unique integer ID for request
    """
    conn = pg.connect(database="ngot", host="127.0.0.1", port="5432")
    cursor = conn.cursor()
    cursor.execute("DELETE from requests WHERE request_id=%s", (request_id,))
    rows = cursor.rowcount
    conn.commit()
    cursor.close()
    conn.close()

    if rows > 0: 
        return jsonify({'result': True})
    return jsonify({'result:': False})

@app.errorhandler(404)
def not_found(error):
    return make_response(jsonify({'error': 'Not found'}), 404)


if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)
    #app.run(debug=True)
