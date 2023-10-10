import random
import datetime

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

start_time = datetime.datetime(2023, 9, 27, 0, 0, 0)
end_time = datetime.datetime(2023, 10, 4, 0, 0, 0)
current_time = start_time
num_seconds_per_week = (end_time - start_time).total_seconds()

with open("backend/arduino_data.txt", "w") as file:
    while current_time < end_time:
        temperature = generate_temperature(current_time.hour)
        entry = f"{current_time.strftime('%Y-%m-%d %H:%M:%S')} {temperature}\n"
        file.write(entry)
        current_time += datetime.timedelta(seconds=1)