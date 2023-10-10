import random
import datetime
import json
from collections import defaultdict

def celsius_to_fahrenheit(celsius):
    return int((celsius * 9/5) + 32)

def generate_temperature(hour):
    if 0 <= hour < 6:
        temperature_celsius = random.randint(13, 17)
    elif 6 <= hour < 9:
        temperature_celsius = random.randint(18, 22)
    elif 9 <= hour < 14:
        temperature_celsius = random.randint(24, 27)
    elif 14 <= hour < 19:
        temperature_celsius = random.randint(20, 24)
    else:
        temperature_celsius = random.randint(17, 22)
    
    temperature_fahrenheit = celsius_to_fahrenheit(temperature_celsius)
    
    return f"{temperature_celsius} C, {temperature_fahrenheit} F"

def generate_average():
    hourly_averages = defaultdict(lambda: {"celsius": [], "fahrenheit": []})

    with open("Backend/arduino_data.txt", "r") as file:
        for line in file:
            parts = line.split()
            date = parts[0]
            time = parts[1]
            celsius = int(parts[2])
            fahrenheit = int(parts[4])

            hour = time.split(":")[0]
            hourly_averages[(date, hour)]["celsius"].append(celsius)
            hourly_averages[(date, hour)]["fahrenheit"].append(fahrenheit)

    hourly_averages_json = []

    for (date, hour), temperatures in hourly_averages.items():
        average_celsius = sum(temperatures["celsius"]) // len(temperatures["celsius"])
        average_fahrenheit = sum(temperatures["fahrenheit"]) // len(temperatures["fahrenheit"])

        hourly_averages_json.append({
            "date": date,
            "hour": hour,
            "celcius": average_celsius,
            "farenheit": average_fahrenheit
        })

    with open("Backend/hourly_averages.json", "w") as json_file:
        json.dump(hourly_averages_json, json_file, indent=2)

    print("Hourly averages saved in 'hourly_averages.json'.")

def main():
    generate_average()

main()