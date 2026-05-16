# Adding Your Own Tools

The echo tools in `src/tools/echo.py` are the simplest template to copy.
Here's the full pattern in one place.

## 1. Create the tool file

`src/tools/my_tool.py`:

```python
from mcp.server.fastmcp import FastMCP


def register(mcp: FastMCP) -> None:

    @mcp.tool()
    def my_tool(input_text: str, count: int = 1) -> dict:
        """
        Describe what this tool does in plain English.
        Claude uses this docstring to decide when to call the tool.
        Make it specific — vague descriptions lead to wrong tool selection.

        Args:
            input_text: Description of this parameter.
            count: Description of this parameter. Include the default.

        Returns:
            Description of what comes back.
        """
        # Your logic here
        return {"result": input_text * count}
```

## 2. Register it in server.py

```python
from src.tools import echo, database, filesystem, http_api, my_tool  # add import

# ...

my_tool.register(mcp)  # add this line
```

## 3. Add it to src/tools/__init__.py

```python
from src.tools import echo, database, filesystem, http_api, my_tool
```

## 4. Write a test

`tests/test_my_tool.py`:

```python
from mcp.server.fastmcp import FastMCP
from src.tools.my_tool import register
import asyncio


def _call(server, tool_name, **kwargs):
    tool = server._tool_manager._tools[tool_name]
    return asyncio.get_event_loop().run_until_complete(tool.fn(**kwargs))


def test_my_tool_basic():
    server = FastMCP(name="test", version="0.0.1")
    register(server)
    result = _call(server, "my_tool", input_text="hello", count=2)
    assert result["result"] == "hellohello"
```

## Tips

**Return dicts, not strings, for structured data.**
Claude handles dict returns better than long strings when it needs
to reference specific fields downstream.

**Be specific in your docstring.**
"Process text" is bad. "Count word frequency in a block of text and
return the top N words sorted by occurrence" is good.

**Validate inputs early.**
Raise `ValueError` with a clear message rather than letting a cryptic
exception bubble up. Claude will relay the error message back to the user.

**Keep tools focused.**
One tool should do one thing. Avoid optional parameters that
fundamentally change what the tool does — split those into two tools.

**Add a cache if you're hitting an external API.**
Copy the TTL cache pattern from `src/tools/http_api.py`.
A 5-minute cache prevents redundant calls during a multi-step conversation.
