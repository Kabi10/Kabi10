"""Tests for echo tools."""

import pytest
from mcp.server.fastmcp import FastMCP
from src.tools.echo import register
from tests.conftest import call_tool


@pytest.fixture
def server():
    s = FastMCP(name="test")
    register(s)
    return s


def test_echo_returns_message(server):
    assert call_tool(server, "echo", message="hello") == "hello"

def test_echo_uppercase(server):
    assert call_tool(server, "echo", message="hello", uppercase=True) == "HELLO"

def test_echo_default_no_uppercase(server):
    assert call_tool(server, "echo", message="hello") == "hello"

def test_echo_empty_string(server):
    assert call_tool(server, "echo", message="") == ""

def test_reverse_basic(server):
    assert call_tool(server, "reverse", text="abcde") == "edcba"

def test_reverse_palindrome(server):
    assert call_tool(server, "reverse", text="racecar") == "racecar"

def test_reverse_empty(server):
    assert call_tool(server, "reverse", text="") == ""

def test_word_count_basic(server):
    result = call_tool(server, "word_count", text="hello world")
    assert result["word_count"] == 2
    assert result["char_count"] == 11
    assert result["char_count_no_spaces"] == 10

def test_word_count_multiline(server):
    result = call_tool(server, "word_count", text="line one\nline two\nline three")
    assert result["line_count"] == 3
    assert result["word_count"] == 6

def test_word_count_empty(server):
    result = call_tool(server, "word_count", text="")
    assert result["word_count"] == 0
    assert result["char_count"] == 0
