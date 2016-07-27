#!flask/bin/python
from flask import Flask, jsonify, abort
from flask import make_response
from flask import request
from flask import url_for
import psycopg2 as pg


app = Flask(__name__)

def make_public_request(request):
    new_request = {}
    new_request['uri'] = url_for('get_requests', request_id=request[0], _external=True)
    new_request['source'] = request[1]
    new_request['destination'] = request[2]

    return new_request

@app.route('/clientapp/requests', methods=['GET'])
def get_requests():
    ''' Get requests from the database
    '''
    conn = pg.connect(database="ngot", host="127.0.0.1", port="5432")
    cursor = conn.cursor()
    cursor.execute("SELECT request_id, source, destination FROM requests")
    rows = list(cursor.fetchall())
    cursor.close()
    conn.close()

    return jsonify({'requests': [make_public_request(req) for req in rows]})

@app.route('/clientapp/vehicle_trips', methods=['GET'])
def get_vehicle_trips():
    ''' Query the database and return generated vehicle trips
    '''
    conn = pg.connect(database="ngot", host="127.0.0.1", port="5432")
    cursor = conn.cursor()
    pg.extensions.register_type(
        pg.extensions.new_array_type(
        (1017,), 'PICKUP_POINTS[]', pg.STRING))
    cursor.execute("SELECT pickup_points FROM vehicletrips")
    rows = cursor.fetchone()
    cursor.close()
    conn.close()

    return jsonify({'vehicle_trips': rows})

@app.route('/clientapp/requests', methods=['POST'])
def create_request():
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

@app.errorhandler(404)
def not_found(error):
    return make_response(jsonify({'error': 'Not found'}), 404)


if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)
    #app.run(debug=True)
