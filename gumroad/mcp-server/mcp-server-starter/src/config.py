from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

    # Auth
    api_key: str = "dev-key-change-me"
    auth_enabled: bool = False  # Disabled by default for easy local dev

    # Transport: "stdio" for Claude Desktop/Cursor, "sse" for HTTP deployment
    transport: str = "stdio"

    # Database
    db_path: str = "./data/data.db"

    # Filesystem tool — all file ops are sandboxed to this directory
    workspace_dir: str = "./workspace"

    # External API
    openweather_api_key: str = ""
    external_api_timeout: int = 10

    # Server metadata
    server_name: str = "mcp-server-starter"
    server_version: str = "1.0.0"


settings = Settings()
