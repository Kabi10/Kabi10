"""Tests for filesystem tools — including sandbox escape prevention."""

import pytest
from tests.conftest import call_tool


def test_list_files_root(mcp_server):
    result = call_tool(mcp_server, "list_files")
    file_names = [f["name"] for f in result["files"]]
    assert "hello.txt" in file_names
    assert "notes.md" in file_names

def test_list_files_shows_subdirs(mcp_server):
    result = call_tool(mcp_server, "list_files")
    dir_names = [d["name"] for d in result["directories"]]
    assert "reports" in dir_names

def test_list_files_subdirectory(mcp_server):
    result = call_tool(mcp_server, "list_files", directory="reports")
    file_names = [f["name"] for f in result["files"]]
    assert "report.txt" in file_names

def test_list_files_nonexistent_raises(mcp_server):
    with pytest.raises(FileNotFoundError):
        call_tool(mcp_server, "list_files", directory="does_not_exist")

def test_read_file_basic(mcp_server):
    result = call_tool(mcp_server, "read_file", path="hello.txt")
    assert result["content"] == "Hello from the workspace!"
    assert result["path"] == "hello.txt"
    assert result["encoding"] == "utf-8"

def test_read_file_not_found_raises(mcp_server):
    with pytest.raises(FileNotFoundError):
        call_tool(mcp_server, "read_file", path="ghost.txt")

def test_write_and_read_file(mcp_server):
    content = "Written by pytest."
    call_tool(mcp_server, "write_file", path="pytest_output.txt", content=content)
    result = call_tool(mcp_server, "read_file", path="pytest_output.txt")
    assert result["content"] == content

def test_write_file_no_overwrite_raises(mcp_server):
    call_tool(mcp_server, "write_file", path="no_overwrite.txt", content="original")
    with pytest.raises(FileExistsError):
        call_tool(mcp_server, "write_file", path="no_overwrite.txt", content="new")

def test_write_file_overwrite_succeeds(mcp_server):
    call_tool(mcp_server, "write_file", path="overwrite_me.txt", content="v1")
    call_tool(mcp_server, "write_file", path="overwrite_me.txt", content="v2", overwrite=True)
    result = call_tool(mcp_server, "read_file", path="overwrite_me.txt")
    assert result["content"] == "v2"

def test_write_creates_nested_dirs(mcp_server):
    call_tool(mcp_server, "write_file", path="deep/nested/file.txt", content="nested")
    result = call_tool(mcp_server, "read_file", path="deep/nested/file.txt")
    assert result["content"] == "nested"

def test_delete_file(mcp_server):
    call_tool(mcp_server, "write_file", path="delete_me.txt", content="bye")
    result = call_tool(mcp_server, "delete_file", path="delete_me.txt")
    assert result["deleted"] is True
    with pytest.raises(FileNotFoundError):
        call_tool(mcp_server, "read_file", path="delete_me.txt")

def test_delete_nonexistent_raises(mcp_server):
    with pytest.raises(FileNotFoundError):
        call_tool(mcp_server, "delete_file", path="does_not_exist.txt")

def test_file_info(mcp_server):
    result = call_tool(mcp_server, "file_info", path="hello.txt")
    assert result["is_file"] is True
    assert result["is_directory"] is False
    assert result["size_bytes"] > 0

# --- Sandbox escape tests ---

def test_path_traversal_blocked(mcp_server):
    with pytest.raises(ValueError, match="outside the workspace"):
        call_tool(mcp_server, "read_file", path="../../../etc/passwd")

def test_absolute_path_blocked(mcp_server):
    with pytest.raises(ValueError, match="outside the workspace"):
        call_tool(mcp_server, "read_file", path="/etc/passwd")

def test_double_dot_in_middle_blocked(mcp_server):
    with pytest.raises(ValueError, match="outside the workspace"):
        call_tool(mcp_server, "read_file", path="reports/../../secret.txt")
