#!/usr/bin/env python3
"""
sensor_simulator.py — Cold Room IoT Sensor Simulator
=====================================================
Simulates 4 sensors (temperature, humidity, pressure, door) and POSTs
readings to the Spring Boot backend every 10 seconds.

Normal ranges (matching database seed data):
  Temperature : -18 °C  ± 2 °C   (anomaly: -10 °C, 5 % chance)
  Humidity    :  50 %   ± 5 %    (anomaly:  70 %,  5 % chance)
  Pressure    : 1013 hPa ± 5 hPa (no anomaly — stays in range)
  Door        :  0 (closed) 95 %, 1 (open) 5 %

Usage:
    python sensor_simulator.py [--url URL] [--interval SECONDS] [--verbose]

Requirements:
    pip install requests
"""

import argparse
import logging
import random
import sys
import time
from datetime import datetime, timezone

def _require_requests():
    """Lazily import requests and exit with a helpful message if missing."""
    try:
        import requests as _requests
        return _requests
    except ImportError:
        print("ERROR: 'requests' library not found. Run:  pip install requests")
        sys.exit(1)


# ── Configuration ─────────────────────────────────────────────────────────────

DEFAULT_API_URL  = "http://localhost:8080/api"
DEFAULT_INTERVAL = 10          # seconds between readings
ANOMALY_CHANCE   = 0.05        # 5 % probability of an anomaly reading
REQUEST_TIMEOUT  = 10          # seconds before giving up on a POST


# ── Sensor definitions ────────────────────────────────────────────────────────

# Each entry maps a sensor_type to its generation parameters.
# 'db_id' is the primary key assigned by the seed data (sensors table).
SENSORS = [
    {
        "db_id":       1,
        "name":        "Temperature Sensor",
        "sensor_type": "temperature",
        "unit":        "°C",
        # Normal: Gaussian around mean with given std-dev
        "normal_mean": -18.0,
        "normal_std":    2.0,
        # Anomaly: fixed value that exceeds the max threshold (-15 °C)
        "anomaly_value": -10.0,
        "has_anomaly":  True,
    },
    {
        "db_id":       2,
        "name":        "Humidity Sensor",
        "sensor_type": "humidity",
        "unit":        "%",
        "normal_mean":  50.0,
        "normal_std":    5.0,
        "anomaly_value": 70.0,   # exceeds max threshold (60 %)
        "has_anomaly":  True,
    },
    {
        "db_id":       3,
        "name":        "Door Sensor",
        "sensor_type": "door",
        "unit":        "boolean",
        # Door is binary: 0 = closed, 1 = open
        "normal_mean":  None,
        "normal_std":   None,
        "anomaly_value": None,
        "has_anomaly":  False,
    },
    {
        "db_id":       4,
        "name":        "Pressure Sensor",
        "sensor_type": "pressure",
        "unit":        "hPa",
        "normal_mean": 1013.0,
        "normal_std":     5.0,
        "anomaly_value": None,
        "has_anomaly":  False,
    },
]


# ── Value generation ──────────────────────────────────────────────────────────

def generate_value(sensor: dict) -> float:
    """
    Returns a simulated reading value for the given sensor definition.

    - Door sensor: 0 (closed) with 95 % probability, 1 (open) with 5 %.
    - Other sensors: Gaussian noise around the normal mean, with a 5 %
      chance of returning the anomaly value when has_anomaly is True.
    """
    if sensor["sensor_type"] == "door":
        return 1.0 if random.random() < ANOMALY_CHANCE else 0.0

    if sensor["has_anomaly"] and random.random() < ANOMALY_CHANCE:
        return sensor["anomaly_value"]

    # Gaussian reading, rounded to 2 decimal places
    value = random.gauss(sensor["normal_mean"], sensor["normal_std"])
    return round(value, 2)


# ── API communication ─────────────────────────────────────────────────────────

def post_reading(api_url: str, sensor: dict, value: float, verbose: bool) -> bool:
    """
    POSTs a single reading to POST /api/readings.

    Returns True on success, False on failure.
    """
    requests = _require_requests()
    timestamp = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%S")
    payload = {
        "sensorId":  sensor["db_id"],
        "value":     value,
        "timestamp": timestamp,
    }
    endpoint = f"{api_url}/readings"

    try:
        response = requests.post(
            endpoint,
            json=payload,
            timeout=REQUEST_TIMEOUT,
            headers={"Content-Type": "application/json", "Accept": "application/json"},
        )
        response.raise_for_status()

        if verbose:
            logging.info(
                "  [%s] %s = %s %s  →  HTTP %d",
                timestamp,
                sensor["name"],
                value,
                sensor["unit"],
                response.status_code,
            )
        return True

    except requests.exceptions.ConnectionError:
        logging.warning(
            "  Connection refused — is the backend running at %s?", api_url
        )
        return False
    except requests.exceptions.Timeout:
        logging.warning("  Request timed out for sensor '%s'", sensor["name"])
        return False
    except requests.exceptions.HTTPError as exc:
        logging.warning(
            "  HTTP error for sensor '%s': %s", sensor["name"], exc.response.text
        )
        return False
    except Exception as exc:  # pylint: disable=broad-except
        logging.warning("  Unexpected error for sensor '%s': %s", sensor["name"], exc)
        return False


def check_health(api_url: str) -> bool:
    """Pings GET /health and returns True if the backend is reachable."""
    requests = _require_requests()
    try:
        resp = requests.get(
            api_url.replace("/api", "") + "/health",
            timeout=5,
        )
        return resp.status_code == 200
    except Exception:  # pylint: disable=broad-except
        return False


# ── Main loop ─────────────────────────────────────────────────────────────────

def run(api_url: str, interval: int, verbose: bool) -> None:
    """
    Main simulation loop.

    Sends one reading per sensor every `interval` seconds.
    Logs a summary line after each round.
    """
    logging.info("=" * 60)
    logging.info("Cold Room Sensor Simulator")
    logging.info("  Backend : %s", api_url)
    logging.info("  Interval: %d seconds", interval)
    logging.info("  Sensors : %d", len(SENSORS))
    logging.info("  Anomaly : %.0f%% chance per reading", ANOMALY_CHANCE * 100)
    logging.info("=" * 60)

    # Wait for backend to be ready
    logging.info("Checking backend connectivity...")
    while not check_health(api_url):
        logging.warning(
            "Backend not reachable at %s — retrying in 5 s...", api_url
        )
        time.sleep(5)
    logging.info("Backend is up. Starting simulation.\n")

    round_num = 0
    while True:
        round_num += 1
        logging.info("── Round %d ──────────────────────────────────────────", round_num)

        successes = 0
        for sensor in SENSORS:
            value   = generate_value(sensor)
            success = post_reading(api_url, sensor, value, verbose)
            if success:
                successes += 1
            if not verbose:
                status = "OK" if success else "FAIL"
                logging.info(
                    "  %-22s  %8.2f %-8s  [%s]",
                    sensor["name"],
                    value,
                    sensor["unit"],
                    status,
                )

        logging.info(
            "  Sent %d/%d readings — sleeping %d s\n",
            successes, len(SENSORS), interval,
        )
        time.sleep(interval)


# ── Entry point ───────────────────────────────────────────────────────────────

def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Cold Room IoT Sensor Simulator",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )
    parser.add_argument(
        "--url",
        default=DEFAULT_API_URL,
        help="Base URL of the Spring Boot API (without trailing slash)",
    )
    parser.add_argument(
        "--interval",
        type=int,
        default=DEFAULT_INTERVAL,
        help="Seconds between each round of readings",
    )
    parser.add_argument(
        "--verbose",
        action="store_true",
        help="Print full request details including HTTP status codes",
    )
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
        logging.info("\nSimulator stopped by user.")
        sys.exit(0)
