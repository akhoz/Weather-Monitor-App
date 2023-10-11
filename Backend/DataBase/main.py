import firebase_handler
import json

def upload_temperatures():
    with open("Backend/Data/hourly_averages.json", "r") as file:
        hourly_averages = json.load(file)
        for dictionary in hourly_averages:
            firebase_handler.upload_file(dictionary)

upload_temperatures()