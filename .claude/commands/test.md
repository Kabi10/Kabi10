# Test Command

**Command**: `/test`

**Description**: Run the full test suite for Android and backend

**Action**:
```bash
cd C:/Dev/Agrimarket && ./gradlew testDebugUnitTest && cd backend && npm test
```

This command runs:
1. Android unit tests via Gradle
2. Backend tests via npm

Results will be displayed in the terminal output.
