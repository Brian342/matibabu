import requests

url = "https://play.im.dhis2.org/stable-2-40-12/api/organisationUnits.json"

params = {
    "fields": "id,displayName",
    "pageSize": 5
}
headers = {
    "Accept": "application/json",
    "X-Requested-with": "XMLHttpRequest"
}

try:
    response = requests.get(
        url,
        auth=("admin", "district"),
        headers =headers,
        params=params
    )
    response.raise_for_status()  # Raise an exception for HTTP errors
    data = response.json()
    print("Org Units:", data["organisationUnits"])
except requests.exceptions.RequestException as e:
    print("Error occurred:", e)