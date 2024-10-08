from flask import Flask, jsonify
import billboard

app = Flask(__name__)

@app.route('/api/billboard/top100', methods=['GET'])
def get_billboard_top100():
    chart = billboard.ChartData('hot-100')
    top100 = [{'rank': song.rank, 'title': song.title, 'artist': song.artist} for song in chart]
    return jsonify(top100)

if __name__ == '__main__':
    app.run(port=5000)
