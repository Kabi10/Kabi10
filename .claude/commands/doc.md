# Documentation Command

**Command**: `/doc [file_path]`

**Description**: Analyze a file and add comprehensive inline comments

**Purpose**: Adds detailed documentation comments to code files for better understanding and maintainability

**Usage**:
```
/doc app/src/main/java/com/senthapps/slagrimarket/ui/home/HomeViewModel.kt
```

**What it does**:
1. Reads the specified file
2. Analyzes the code structure (classes, functions, logic)
3. Adds KDoc/JSDoc style comments explaining:
   - Class purpose and responsibilities
   - Function parameters, return values, and behavior
   - Complex logic and algorithms
   - Edge cases and error handling
4. Follows project coding standards (MVVM patterns, offline-first, etc.)

**Best for**:
- Complex ViewModels and repositories
- Business logic and algorithms
- API service interfaces
- Data transformation utilities
