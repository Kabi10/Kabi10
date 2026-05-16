"""
Tool: filesystem

Demonstrates safe file system access from an MCP tool.

Patterns demonstrated:
- Directory sandboxing (all ops confined to workspace/)
- Path traversal prevention
- File size limits on write
- Metadata vs content separation
- Graceful error messages (buyers will copy these patterns)

All operations are confined to the WORKSPACE_DIR setting (default: ./workspace).
Attempts to escape the sandbox via ../ or absolute paths are blocked.
"""

import os
from pathlib import Path
from mcp.server.fastmcp import FastMCP

from src.config import settings

MAX_READ_BYTES = 1_000_000   # 1 MB
MAX_WRITE_BYTES = 500_000    # 500 KB
MAX_LIST_DEPTH = 3


def _get_workspace() -> Path:
    workspace = Path(settings.workspace_dir).resolve()
    workspace.mkdir(parents=True, exist_ok=True)
    return workspace


def _safe_path(relative_path: str) -> Path:
    """
    Resolve a relative path within the workspace.
    Raises ValueError if the path escapes the sandbox.
    """
    workspace = _get_workspace()
    # Resolve to absolute, then verify it's inside workspace
    target = (workspace / relative_path).resolve()
    # Use is_relative_to (3.9+) for exact boundary check.
    # str-startswith would pass /tmp/workspace_evil when workspace=/tmp/workspace.
    try:
        target.relative_to(workspace)
    except ValueError:
        raise ValueError(
            f"Path '{relative_path}' is outside the workspace directory. "
            "All file operations must stay within the workspace."
        )
    return target


def register(mcp: FastMCP) -> None:
    """Register filesystem tools with the MCP server."""

    @mcp.tool()
    def list_files(directory: str = ".") -> dict:
        """
        List files and subdirectories in the workspace.

        Args:
            directory: Relative path within the workspace to list.
                       Defaults to the workspace root.

        Returns:
            Dict with 'directory', 'files', and 'directories' keys.
        """
        target = _safe_path(directory)

        if not target.exists():
            raise FileNotFoundError(f"Directory '{directory}' does not exist in workspace")
        if not target.is_dir():
            raise ValueError(f"'{directory}' is a file, not a directory")

        files = []
        dirs = []

        for item in sorted(target.iterdir()):
            rel = str(item.relative_to(_get_workspace()))
            if item.is_file():
                files.append({
                    "name": item.name,
                    "path": rel,
                    "size_bytes": item.stat().st_size,
                    "extension": item.suffix,
                })
            elif item.is_dir():
                dirs.append({
                    "name": item.name,
                    "path": rel,
                })

        return {
            "directory": directory,
            "files": files,
            "directories": dirs,
            "total_files": len(files),
            "total_dirs": len(dirs),
        }

    @mcp.tool()
    def read_file(path: str) -> dict:
        """
        Read the contents of a file in the workspace.

        Args:
            path: Relative path to the file within the workspace.
                  e.g. "notes.txt" or "reports/summary.md"

        Returns:
            Dict with 'path', 'content', 'size_bytes', and 'encoding' keys.
        """
        target = _safe_path(path)

        if not target.exists():
            raise FileNotFoundError(f"File '{path}' not found in workspace")
        if not target.is_file():
            raise ValueError(f"'{path}' is a directory, not a file")

        size = target.stat().st_size
        if size > MAX_READ_BYTES:
            raise ValueError(
                f"File is {size:,} bytes — exceeds the {MAX_READ_BYTES:,} byte read limit. "
                "Consider reading a smaller file or splitting it."
            )

        # Try UTF-8 first, fall back to latin-1 for binary-ish files
        try:
            content = target.read_text(encoding="utf-8")
            encoding = "utf-8"
        except UnicodeDecodeError:
            content = target.read_text(encoding="latin-1")
            encoding = "latin-1"

        return {
            "path": path,
            "content": content,
            "size_bytes": size,
            "encoding": encoding,
        }

    @mcp.tool()
    def write_file(path: str, content: str, overwrite: bool = False) -> dict:
        """
        Write text content to a file in the workspace.
        Creates parent directories automatically.

        Args:
            path: Relative path for the new file within the workspace.
                  e.g. "output.txt" or "reports/result.md"
            content: Text content to write.
            overwrite: If False (default), raises an error if file already exists.
                       Set to True to replace existing files.

        Returns:
            Dict with 'path', 'size_bytes', and 'created' keys.
        """
        if len(content.encode("utf-8")) > MAX_WRITE_BYTES:
            raise ValueError(
                f"Content is {len(content.encode()):,} bytes — "
                f"exceeds the {MAX_WRITE_BYTES:,} byte write limit."
            )

        target = _safe_path(path)

        if target.exists() and not overwrite:
            raise FileExistsError(
                f"File '{path}' already exists. "
                "Set overwrite=True to replace it."
            )

        target.parent.mkdir(parents=True, exist_ok=True)
        is_new = not target.exists()
        target.write_text(content, encoding="utf-8")

        return {
            "path": path,
            "size_bytes": target.stat().st_size,
            "created": is_new,
            "message": f"File written successfully: {path}",
        }

    @mcp.tool()
    def delete_file(path: str) -> dict:
        """
        Delete a file from the workspace.

        Args:
            path: Relative path to the file within the workspace.

        Returns:
            Dict with 'path' and 'deleted' keys.
        """
        target = _safe_path(path)

        if not target.exists():
            raise FileNotFoundError(f"File '{path}' not found in workspace")
        if target.is_dir():
            raise ValueError(
                f"'{path}' is a directory. This tool only deletes files. "
                "Remove files inside it first."
            )

        target.unlink()
        return {
            "path": path,
            "deleted": True,
            "message": f"File deleted: {path}",
        }

    @mcp.tool()
    def file_info(path: str) -> dict:
        """
        Get metadata about a file without reading its full content.
        Useful for large files where you only need size/type info.

        Args:
            path: Relative path to the file within the workspace.

        Returns:
            Dict with file metadata.
        """
        target = _safe_path(path)

        if not target.exists():
            raise FileNotFoundError(f"'{path}' not found in workspace")

        stat = target.stat()
        return {
            "path": path,
            "name": target.name,
            "extension": target.suffix,
            "size_bytes": stat.st_size,
            "is_file": target.is_file(),
            "is_directory": target.is_dir(),
            "readable": os.access(target, os.R_OK),
            "writable": os.access(target, os.W_OK),
            "within_read_limit": stat.st_size <= MAX_READ_BYTES,
        }
