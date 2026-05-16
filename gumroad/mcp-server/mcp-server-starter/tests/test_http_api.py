"""Tests for HTTP API tools — uses mocking to avoid real network calls."""

import pytest
from unittest.mock import patch
from tests.conftest import call_tool

MOCK_GEO_RESPONSE = {
    "results": [{"name": "Vancouver", "latitude": 49.2827, "longitude": -123.1207, "country": "Canada"}]
}
MOCK_WEATHER_RESPONSE = {
    "current": {"temperature_2m": 12.5, "relative_humidity_2m": 78, "wind_speed_10m": 15.2, "weather_code": 3}
}
MOCK_COUNTRY_RESPONSE = [{
    "name": {"common": "Canada", "official": "Canada"},
    "capital": ["Ottawa"],
    "region": "Americas",
    "subregion": "North America",
    "population": 38000000,
    "languages": {"eng": "English", "fra": "French"},
    "currencies": {"CAD": {"name": "Canadian dollar", "symbol": "$"}},
    "area": 9984670,
    "timezones": ["UTC-05:00"],
}]


@patch("src.tools.http_api._request_with_retry")
def test_get_weather_returns_expected_fields(mock_req, mcp_server):
    import src.tools.http_api as m; m._cache.clear()
    mock_req.side_effect = [MOCK_GEO_RESPONSE, MOCK_WEATHER_RESPONSE]
    result = call_tool(mcp_server, "get_current_weather", city="Vancouver")
    assert result["city"] == "Vancouver"
    assert result["temperature"] == 12.5
    assert result["humidity_percent"] == 78
    assert result["condition"] == "Overcast"
    assert result["cached"] is False


@patch("src.tools.http_api._request_with_retry")
def test_get_weather_caches_result(mock_req, mcp_server):
    import src.tools.http_api as m; m._cache.clear()
    mock_req.side_effect = [MOCK_GEO_RESPONSE, MOCK_WEATHER_RESPONSE]
    call_tool(mcp_server, "get_current_weather", city="CacheTest")
    result2 = call_tool(mcp_server, "get_current_weather", city="CacheTest")
    assert mock_req.call_count == 2  # Only called on first request
    assert result2["cached"] is True


@patch("src.tools.http_api._request_with_retry")
def test_get_weather_city_not_found_raises(mock_req, mcp_server):
    import src.tools.http_api as m; m._cache.clear()
    mock_req.return_value = {"results": []}
    with pytest.raises(ValueError, match="not found"):
        call_tool(mcp_server, "get_current_weather", city="FakeCity999")


@patch("src.tools.http_api._request_with_retry")
def test_get_country_info(mock_req, mcp_server):
    import src.tools.http_api as m; m._cache.clear()
    mock_req.return_value = MOCK_COUNTRY_RESPONSE
    result = call_tool(mcp_server, "get_country_info", country="Canada")
    assert result["name"] == "Canada"
    assert result["capital"] == "Ottawa"
    assert "English" in result["languages"]


@patch("src.tools.http_api._request_with_retry")
def test_http_get_basic(mock_req, mcp_server):
    mock_req.return_value = {"login": "octocat", "id": 1}
    result = call_tool(mcp_server, "http_get", url="https://api.github.com/users/octocat")
    assert result["status"] == "success"
    assert result["data"]["login"] == "octocat"


def test_http_get_rejects_non_https(mcp_server):
    with pytest.raises(ValueError, match="HTTPS"):
        call_tool(mcp_server, "http_get", url="http://insecure.example.com")


def test_http_get_rejects_plain_url(mcp_server):
    with pytest.raises(ValueError, match="HTTPS"):
        call_tool(mcp_server, "http_get", url="ftp://files.example.com")
