"""Tests for database query tools."""

import pytest
from tests.conftest import call_tool


def test_list_tables(mcp_server):
    result = call_tool(mcp_server, "list_tables")
    assert isinstance(result, list)
    assert "products" in result
    assert "orders" in result

def test_describe_table_products(mcp_server):
    result = call_tool(mcp_server, "describe_table", table="products")
    column_names = [col["column"] for col in result]
    assert "id" in column_names
    assert "name" in column_names
    assert "price" in column_names
    assert "sku" in column_names

def test_query_all_products(mcp_server):
    result = call_tool(mcp_server, "query_table", table="products")
    assert len(result) == 3
    assert all("name" in row for row in result)

def test_query_with_filter(mcp_server):
    result = call_tool(mcp_server, "query_table", table="products", filters={"category": "hardware"})
    assert len(result) == 2
    assert all(row["category"] == "hardware" for row in result)

def test_query_with_limit(mcp_server):
    result = call_tool(mcp_server, "query_table", table="products", limit=1)
    assert len(result) == 1

def test_query_limit_cap(mcp_server):
    result = call_tool(mcp_server, "query_table", table="products", limit=999)
    assert len(result) <= 100

def test_query_specific_columns(mcp_server):
    result = call_tool(mcp_server, "query_table", table="products", columns=["name", "price"])
    assert len(result) > 0
    assert "name" in result[0]
    assert "price" in result[0]
    assert "sku" not in result[0]

def test_query_order_by_desc(mcp_server):
    result = call_tool(mcp_server, "query_table", table="products", order_by="price", order_dir="DESC")
    prices = [row["price"] for row in result]
    assert prices == sorted(prices, reverse=True)

def test_query_order_by_asc(mcp_server):
    result = call_tool(mcp_server, "query_table", table="products", order_by="price", order_dir="ASC")
    prices = [row["price"] for row in result]
    assert prices == sorted(prices)

def test_count_rows_all(mcp_server):
    result = call_tool(mcp_server, "count_rows", table="products")
    assert result["count"] == 3
    assert result["table"] == "products"

def test_count_rows_filtered(mcp_server):
    result = call_tool(mcp_server, "count_rows", table="orders", filters={"status": "pending"})
    assert result["count"] == 2

def test_query_invalid_table_raises(mcp_server):
    with pytest.raises(ValueError, match="not accessible"):
        call_tool(mcp_server, "query_table", table="secret_internal_table")

def test_query_invalid_order_dir_raises(mcp_server):
    with pytest.raises(ValueError, match="order_dir"):
        call_tool(mcp_server, "query_table", table="products", order_by="name", order_dir="SIDEWAYS")

def test_describe_invalid_table_raises(mcp_server):
    with pytest.raises(ValueError):
        call_tool(mcp_server, "describe_table", table="not_a_real_table")
