import os
import json
import firebase_admin
from firebase_functions import db_fn, https_fn  # Cloud functions for firebase SDK
from firebase_admin import credentials, storage, db, initialize_app
from firebase.firebase import FirebaseApplication
from typing import TypedDict, List, Optional, Callable, Any

cred = credentials.Certificate("Backend/DataBase/firebase_credentials.json")
app = initialize_app(
    cred,
    {
        "databaseURL": "https://weathermonitordb-default-rtdb.firebaseio.com/",
    },
)

def upload_file(dictionary):
    try:
        temperature_branch = db.reference("Temperature")
        date = dictionary["date"]
        hour = dictionary["hour"]
        day_report = temperature_branch.child(date).get()
        print(day_report)
        if day_report is None:
            day_report = temperature_branch.child(date)
            day_report.update({hour: dictionary})
            
        if hour not in day_report:
            day_report = temperature_branch.child(date)
            day_report.update({hour: dictionary})
        day_report = day_report.child(hour)
        day_report.update(dictionary)

    except Exception as e:
        print(e)