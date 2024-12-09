from flask import Flask, jsonify
import requests
from bs4 import BeautifulSoup
import json
from urllib.parse import quote

app = Flask(__name__)

# JSON 파일 경로
json_file_path = r'C:\start_with_none 2학기\firstproject\scripts\billboard_Hot100.json'

def get_spotify_access_token():
    CLIENT_ID = "8c95a70668cf40edb931e9ba2e9c50d9"
    CLIENT_SECRET = "980aec70360644ce84c74ff4fc4b7f51"

    url = "https://accounts.spotify.com/api/token"
    headers = {
        'Content-Type': 'application/x-www-form-urlencoded'
    }
    data = {
        'grant_type': 'client_credentials',
        'client_id': CLIENT_ID,
        'client_secret': CLIENT_SECRET
    }

    response = requests.post(url, headers=headers, data=data)

    if response.status_code == 200:
        return response.json()['access_token']
    else:
        print("Access token을 가져오는 중 오류 발생:", response.status_code, response.text)
        return None

def fetch_billboard_top100():
    try:
        print("Billboard Hot 100을 가져오는 중...")
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
        }
        response = requests.get("https://www.billboard.com/charts/hot-100", headers=headers)

        if response.status_code == 200:
            print("Billboard 응답 수신 완료.")
            soup = BeautifulSoup(response.text, 'html.parser')
            songs = []

            for index, item in enumerate(soup.select('.o-chart-results-list-row-container'), start=1):
                artist = item.select_one('.c-label')
                title = item.select_one('.c-title')

                access_token = get_spotify_access_token()
                album_image_url = None
                composer_name = None

                if access_token and title:
                    album_title = quote(title.text.strip())
                    search_url = f"https://api.spotify.com/v1/search?q={album_title}&type=track"
                    headers = {
                        'Authorization': f'Bearer {access_token}'
                    }

                    search_response = requests.get(search_url, headers=headers)

                    if search_response.status_code == 200:
                        search_data = search_response.json()
                        if search_data['tracks']['items']:
                            track = search_data['tracks']['items'][0]
                            album_image_url = track['album']['images'][0]['url']
                            composers = track['artists']
                            composer_name = ', '.join(artist['name'] for artist in composers)

                if title:
                    song = {
                        'rank': index,
                        'trackName': title.text.strip(),
                        'albumImage': album_image_url,
                        'composer': composer_name
                    }
                    songs.append(song)

            with open(json_file_path, 'w', encoding='utf-8') as json_file:
                json.dump(songs, json_file, ensure_ascii=False, indent=4)

            print(f"총 {len(songs)} 곡을 성공적으로 가져오고 저장했습니다.")
            return jsonify(songs)
        else:
            print(f"Billboard 데이터 가져오기 오류: {response.status_code}")
            return jsonify({"error": "데이터 가져오기 실패"}), 500
    except Exception as e:
        print(f"예외 발생: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/api/billboard/top100', methods=['GET'])
def get_billboard_top100():
    return fetch_billboard_top100()

if __name__ == '__main__':
    app.run(debug=True)

# http://127.0.0.1:5000/api/billboard/top100