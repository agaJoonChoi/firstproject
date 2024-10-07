# 필요한 라이브러리 설치
# pip install flask

from flask import Flask, jsonify
import billboard

app = Flask(__name__)

@app.route('/billboard', methods=['GET'])
def get_billboard_chart():
    chart = billboard.ChartData('hot-100')
    chart_data = []

    for song in chart:
        chart_data.append({
            'title': song.title,
            'artist': song.artist,
            'rank': song.rank
        })

    return jsonify(chart_data)

if __name__ == "__main__":
    app.run(debug=True)
