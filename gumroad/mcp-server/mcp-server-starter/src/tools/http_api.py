"""
Tool: http_api

Demonstrates how to wrap an external REST API as MCP tools.

Patterns demonstrated:
- Injecting external API keys from config (separate from MCP auth)
- Timeout handling
- Retry with exponential backoff on rate limit / server errors
- Simple in-memory TTL cache to avoid redundant API calls
- Structured error messages that help the AI understand what went wrong

Uses the Open-Meteo weather API (no API key required) as the default example,
so the tool works out of the box with zero config.

Also includes a generic http_get tool that buyers can use to call
any REST API without writing a wrapper.
"""

import time
import httpx
from mcp.server.fastmcp import FastMCP

from src.config import settings

# Simple in-memory TTL cache: {cache_key: (timestamp, data)}
_cache: dict[str, tuple[float, dict]] = {}
CACHE_TTL_SECONDS = 300  # 5 minutes


def _cache_get(key: str) -> dict | None:
    if key in _cache:
        ts, data = _cache[key]
        if time.time() - ts < CACHE_TTL_SECONDS:
            return data
        del _cache[key]
    return None


def _cache_set(key: str, data: dict) -> None:
    _cache[key] = (time.time(), data)


def _request_with_retry(
    url: str,
    params: dict | None = None,
    headers: dict | None = None,
    max_retries: int = 3,
) -> dict:
    """
    Make an HTTP GET request with exponential backoff on 429/5xx.
    Returns parsed JSON or raises an exception with a clear message.
    """
    last_error: Exception | None = None

    for attempt in range(max_retries):
        try:
            with httpx.Client(timeout=settings.external_api_timeout) as client:
                response = client.get(url, params=params, headers=headers)

            if response.status_code == 200:
                return response.json()

            if response.status_code == 429 or response.status_code >= 500:
                # Retryable: rate limit or server error
                wait = 2 ** attempt
                time.sleep(wait)
                last_error = Exception(
                    f"HTTP {response.status_code} on attempt {attempt + 1}. "
                    f"Retrying in {wait}s..."
                )
                continue

            # Non-retryable client error
            raise ValueError(
                f"API returned HTTP {response.status_code}: {response.text[:200]}"
            )

        except httpx.TimeoutException:
            last_error = TimeoutError(
                f"Request timed out after {settings.external_api_timeout}s "
                f"(attempt {attempt + 1}/{max_retries})"
            )
            time.sleep(2 ** attempt)

        except httpx.RequestError as e:
            raise ConnectionError(f"Network error calling {url}: {e}") from e

    raise last_error or Exception(f"All {max_retries} attempts failed for {url}")


def register(mcp: FastMCP) -> None:
    """Register HTTP API tools with the MCP server."""

    @mcp.tool()
    def get_current_weather(
        city: str,
        country_code: str = "",
        units: str = "celsius",
    ) -> dict:
        """
        Get current weather conditions for a city.

        Uses the Open-Meteo geocoding + weather APIs (no API key required).

        Args:
            city: City name. e.g. "Vancouver", "Tokyo", "London"
            country_code: Optional ISO country code to disambiguate.
                          e.g. "CA" for Canada, "GB" for UK
            units: Temperature unit — "celsius" or "fahrenheit"

        Returns:
            Dict with temperature, weather description, wind, humidity.
        """
        cache_key = f"weather:{city}:{country_code}:{units}"
        cached = _cache_get(cache_key)
        if cached:
            return {**cached, "cached": True}

        # Step 1: Geocode the city name
        geo_params: dict = {"name": city, "count": 1, "language": "en", "format": "json"}
        if country_code:
            geo_params["countryCode"] = country_code.upper()

        geo_data = _request_with_retry(
            "https://geocoding-api.open-meteo.com/v1/search",
            params=geo_params,
        )

        results = geo_data.get("results", [])
        if not results:
            raise ValueError(
                f"City '{city}' not found. "
                "Try adding a country_code (e.g. country_code='CA' for Canada)."
            )

        location = results[0]
        lat, lon = location["latitude"], location["longitude"]

        # Step 2: Fetch weather
        temp_unit = "fahrenheit" if units == "fahrenheit" else "celsius"
        weather_data = _request_with_retry(
            "https://api.open-meteo.com/v1/forecast",
            params={
                "latitude": lat,
                "longitude": lon,
                "current": "temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code",
                "temperature_unit": temp_unit,
                "wind_speed_unit": "kmh",
                "timezone": "auto",
            },
        )

        current = weather_data.get("current", {})
        weather_code = current.get("weather_code", 0)

        result = {
            "city": location.get("name", city),
            "country": location.get("country", ""),
            "latitude": lat,
            "longitude": lon,
            "temperature": current.get("temperature_2m"),
            "temperature_unit": "°C" if units == "celsius" else "°F",
            "humidity_percent": current.get("relative_humidity_2m"),
            "wind_speed_kmh": current.get("wind_speed_10m"),
            "condition": _weather_code_to_description(weather_code),
            "weather_code": weather_code,
            "cached": False,
        }

        _cache_set(cache_key, result)
        return result

    @mcp.tool()
    def get_country_info(country: str) -> dict:
        """
        Get information about a country by name or ISO code.

        Uses the RestCountries API (no API key required).

        Args:
            country: Country name or ISO 2-letter code.
                     e.g. "Canada", "CA", "Japan", "JP"

        Returns:
            Dict with capital, population, languages, currencies, region.
        """
        cache_key = f"country:{country.lower()}"
        cached = _cache_get(cache_key)
        if cached:
            return {**cached, "cached": True}

        # Try name search first, fall back to alpha code
        try:
            if len(country) == 2:
                url = f"https://restcountries.com/v3.1/alpha/{country.lower()}"
                data = _request_with_retry(url)
                if isinstance(data, list):
                    data = data[0]
            else:
                url = f"https://restcountries.com/v3.1/name/{country}"
                data = _request_with_retry(url)
                data = data[0] if isinstance(data, list) else data
        except ValueError as e:
            raise ValueError(
                f"Country '{country}' not found. "
                "Try the full country name or ISO 2-letter code (e.g. 'CA', 'JP')."
            ) from e

        languages = list(data.get("languages", {}).values())
        currencies = [
            f"{v.get('name', k)} ({v.get('symbol', '')})"
            for k, v in data.get("currencies", {}).items()
        ]

        result = {
            "name": data.get("name", {}).get("common", country),
            "official_name": data.get("name", {}).get("official", ""),
            "capital": data.get("capital", [""])[0] if data.get("capital") else "",
            "region": data.get("region", ""),
            "subregion": data.get("subregion", ""),
            "population": data.get("population", 0),
            "languages": languages,
            "currencies": currencies,
            "area_km2": data.get("area", 0),
            "timezones": data.get("timezones", []),
            "cached": False,
        }

        _cache_set(cache_key, result)
        return result

    @mcp.tool()
    def http_get(
        url: str,
        params: dict | None = None,
        headers: dict | None = None,
    ) -> dict:
        """
        Make a generic HTTP GET request to any public API endpoint.

        Use this when you need to call an API that doesn't have a
        dedicated tool in this server. Returns the parsed JSON response.

        Args:
            url: Full URL to request. Must start with https://.
            params: Optional query parameters as a dict.
            headers: Optional request headers as a dict.
                     Use this for API key auth:
                     {"Authorization": "Bearer YOUR_KEY"}

        Returns:
            Dict with 'status', 'url', and 'data' keys.

        Example:
            http_get(
                url="https://api.github.com/users/octocat",
                headers={"Accept": "application/vnd.github.v3+json"}
            )
        """
        if not url.startswith("https://"):
            raise ValueError(
                "Only HTTPS URLs are allowed for security. "
                "Got: " + url[:50]
            )

        data = _request_with_retry(url, params=params, headers=headers)
        return {
            "status": "success",
            "url": url,
            "data": data,
        }


def _weather_code_to_description(code: int) -> str:
    """Map WMO weather codes to human-readable descriptions."""
    codes = {
        0: "Clear sky",
        1: "Mainly clear", 2: "Partly cloudy", 3: "Overcast",
        45: "Foggy", 48: "Icy fog",
        51: "Light drizzle", 53: "Moderate drizzle", 55: "Heavy drizzle",
        61: "Light rain", 63: "Moderate rain", 65: "Heavy rain",
        71: "Light snow", 73: "Moderate snow", 75: "Heavy snow",
        77: "Snow grains",
        80: "Light showers", 81: "Moderate showers", 82: "Heavy showers",
        85: "Light snow showers", 86: "Heavy snow showers",
        95: "Thunderstorm",
        96: "Thunderstorm with light hail", 99: "Thunderstorm with heavy hail",
    }
    return codes.get(code, f"Unknown (code {code})")
