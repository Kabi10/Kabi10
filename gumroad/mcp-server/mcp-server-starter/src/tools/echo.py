"""
Tool: echo

The simplest possible MCP tool. Use this as your starting template
when adding new tools to this server.

Pattern demonstrated:
- Basic @mcp.tool() decorator usage
- Type-annotated inputs (MCP uses these for schema generation)
- Optional parameters with defaults
- Simple return type
"""

from mcp.server.fastmcp import FastMCP


def register(mcp: FastMCP) -> None:
    """Register echo tools with the MCP server."""

    @mcp.tool()
    def echo(message: str, uppercase: bool = False) -> str:
        """
        Echo a message back. Optionally convert to uppercase.

        Use this tool as a template when building your own tools.
        Everything here is the minimum viable pattern.

        Args:
            message: The text to echo back.
            uppercase: If True, return the message in uppercase.

        Returns:
            The original message, optionally uppercased.
        """
        return message.upper() if uppercase else message

    @mcp.tool()
    def reverse(text: str) -> str:
        """
        Reverse a string.

        Second example showing a pure transformation tool
        with no external dependencies.

        Args:
            text: The string to reverse.

        Returns:
            The reversed string.
        """
        return text[::-1]

    @mcp.tool()
    def word_count(text: str) -> dict:
        """
        Count words, characters, and lines in a block of text.

        Example of a tool that returns a structured dict
        instead of a plain string.

        Args:
            text: The text to analyze.

        Returns:
            Dict with word_count, char_count, line_count keys.
        """
        lines = text.splitlines()
        words = text.split()
        return {
            "word_count": len(words),
            "char_count": len(text),
            "char_count_no_spaces": len(text.replace(" ", "")),
            "line_count": len(lines),
        }
