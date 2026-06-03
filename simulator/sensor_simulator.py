# -*- coding: utf-8 -*-
"""
sensor_simulator.py - Cold Room IoT Sensor Simulator
Simulates 4 sensors and POSTs readings to the Spring Boot backend every 10 seconds.
"""

import argparse
import logging
import random
import sys
import time
from datetime import datetime, timezone

import requests

DEFAULT_API_URL  = "http://localhost:8080/api"
DEFAULT_INTERVAL = 10
ANOMALY_CHANCE   = 0.05
REQUEST_TIMEOUT  = 10

SENSORS = [
    {
        "db_id":         1,
        "name":          "Temperature Sensor",
        "sensor_type":   "temperature",
        "unit":          "degC",
        "normal_mean":   -18.0,
        "normal_std":      2.0,
        "anomaly_value": -10.0,
        "has_anomaly":   True,
    },
    {
        "db_id":         2,
        "name":          "Humidity Sensor",
        "sensor_type":   "humidity",
        "unit":          "%",
        "normal_mean":    50.0,
        "normal_std":      5.0,
        "anomaly_value":  70.0,
        "has_anomaly":   True,
    },
    {
        "db_id":         3,
        "name":          "Door Sensor",
        "sensor_type":   "door",
        "unit":          "boolean",
        "normal_mean":   None,
        "normal_std":    None,
        "anomaly_value": None,
        "has_anomaly":   False,
    },
    {
        "db_id":         4,
        "name":          "Pressure Sensor",
        "sensor_type":   "pressure",
        "unit":          "hPa",
        "normal_mean":   1013.0,
        "normal_std":       5.0,
        "anomaly_value": None,
        "has_anomaly":   False,
    },
]


def generate_value(sensor):
    if sensor["sensor_type"] == "door":
        return 1.0 if random.random() < ANOMALY_CHANCE else 0.0
    if sensor["has_anomaly"] and random.random() < ANOMALY_CHANCE:
        return sensor["anomaly_value"]
    value = random.gauss(sensor["normal_mean"], sensor["normal_std"])
    return round(value, 2)


def post_reading(api_url, sensor, value, verbose):
    timestamp = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%S")
    payload = {
        "sensorId":  sensor["db_id"],
        "value":     value,
        "timestamp": timestamp,
    }
    try:
        response = requests.post(
            f"{api_url}/readings",
            json=payload,
            timeout=REQUEST_TIMEOUT,
            headers={"Content-Type": "application/json", "Accept": "application/json"},
        )
        response.raise_for_status()
        if verbose:
            logging.info("  [%s] %s = %s %s  -> HTTP %d",
                         timestamp, sensor["name"], value, sensor["unit"], response.status_code)
        return True
    except requests.exceptions.ConnectionError:
        logging.warning("  Connection refused - is the backend running at %s?", api_url)
        return False
    except requests.exceptions.Timeout:
        logging.warning("  Request timed out for sensor '%s'", sensor["name"])
        return False
    except requests.exceptions.HTTPError as exc:
        logging.warning("  HTTP error for sensor '%s': %s", sensor["name"], exc.response.text)
        return False
    except Exception as exc:
        logging.warning("  Unexpected error for sensor '%s': %s", sensor["name"], exc)
        return False


def check_health(api_url):
    try:
        resp = requests.get(api_url.replace("/api", "") + "/health", timeout=5)
        return resp.status_code == 200
    except Exception:
        return False


def run(api_url, interval, verbose):
    logging.info("=" * 55)
    logging.info("Cold Room Sensor Simulator")
    logging.info("  Backend : %s", api_url)
    logging.info("  Interval: %d seconds", interval)
    logging.info("  Sensors : %d", len(SENSORS))
    logging.info("  Anomaly : %.0f%% chance per reading", ANOMALY_CHANCE * 100)
    logging.info("=" * 55)

    logging.info("Checking backend connectivity...")
    while not check_health(api_url):
        logging.warning("Backend not reachable at %s - retrying in 5s...", api_url)
        time.sleep(5)
    logging.info("Backend is up. Starting simulation.")

    round_num = 0
    while True:
        round_num += 1
        logging.info("-- Round %d --", round_num)
        successes = 0
        for sensor in SENSORS:
            value = generate_value(sensor)
            success = post_reading(api_url, sensor, value, verbose)
            if success:
                successes += 1
            if not verbose:
                status = "OK" if success else "FAIL"
                logging.info("  %-22s  %8.2f %-8s  [%s]",
                             sensor["name"], value, sensor["unit"], status)
        logging.info("  Sent %d/%d readings - sleeping %ds", successes, len(SENSORS), interval)
        time.sleep(interval)


def parse_args():
    parser = argparse.ArgumentParser(description="Cold Room IoT Sensor Simulator")
    parser.add_argument("--url", default=DEFAULT_API_URL,
                        help="Base URL of the Spring Boot API")
    parser.add_argument("--interval", type=int, default=DEFAULT_INTERVAL,
                        help="Seconds between each round of readings")
    parser.add_argument("--verbose", action="store_true",
                        help="Print full request details")
    return parser.parse_args()


if __name__ == "__main__":
    args = parse_args()
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s  %(message)s",
        datefmt="%H:%M:%S",
        stream=sys.stdout,
    )
    try:
        run(api_url=args.url, interval=args.interval, verbose=args.verbose)
    except KeyboardInterrupt:
        logging.info("Simulator stopped by user.")
        sys.exit(0)
