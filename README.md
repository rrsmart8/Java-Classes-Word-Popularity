
# Java Class Name Popularity Analyzer

This project uses the GitHub API to analyze popular Java-based repositories and extract class names from `.java` files. The goal is to calculate the popularity of words used in class names across these repositories, helping to identify commonly used terms in Java development.

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Requirements](#requirements)
- [Setup Instructions](#setup-instructions)
- [Usage](#usage)
- [Code Structure](#code-structure)
- [Example Output](#example-output)
- [License](#license)

## Overview
This program:
1. **Fetches Java-based repositories** from GitHub using the GitHub API.
2. **Extracts class names** from `.java` files within each repository.
3. **Calculates word popularity** by breaking down class names into individual words.
4. **Displays the top 20 most popular words** based on their frequency across class names.

This analysis provides insights into frequently used terms in Java class naming conventions across popular repositories on GitHub.

## Features
- Fetches the top Java repositories on GitHub based on star count.
- Dynamically adapts to each repository's default branch.
- Analyzes `.java` files to extract class names.
- Calculates and displays the most frequently used words in class names.

## Requirements
- Kotlin 1.5+ with Gradle (or your preferred build tool).
- GitHub API token with appropriate permissions (required to access repository data).

## Setup Instructions

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/yourusername/JavaClassNamePopularityAnalyzer.git
   cd JavaClassNamePopularityAnalyzer
   ```

2. **Install Dependencies**:
   Add the following dependencies in your `build.gradle.kts`:
   ```kotlin
   dependencies {
       implementation("com.squareup.okhttp3:okhttp:4.9.3")
       implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
   }
   ```

3. **Set Up GitHub API Token**:
    - Generate a GitHub personal access token by going to [GitHub’s token settings](https://github.com/settings/tokens).
    - Copy the token and replace `"YOUR_GITHUB_TOKEN"` in the code with your token.

4. **Build the Project**:
   ```bash
   ./gradlew build
   ```

## Usage
Run the program with:
```bash
./gradlew run
```

The program will fetch Java repositories, extract class names from `.java` files, and display the most popular words in those class names.

## Code Structure

### Main Components

- **`searchJavaRepositories`**: Fetches popular Java repositories using the GitHub Search API. The repositories are sorted by star count in descending order.

- **`getLatestCommitSHA`**: Retrieves the latest commit SHA for the specified branch of a repository. It dynamically uses the repository's default branch to avoid issues with varying branch names.

- **`getJavaFiles`**: Retrieves the file tree for a repository and filters out only the `.java` files. These files are used to extract class names.

- **`getClassNamesFromJavaFile`**: For each `.java` file, retrieves the content using the GitHub API, decodes the Base64 content, and extracts class names using a regular expression. The regular expression identifies keywords following `class`.

- **`calculateWordPopularity`**: Splits each class name into words based on CamelCase convention and calculates the frequency of each word across all class names. It stores the word frequencies in a map to determine the most popular terms.

### Code Flow

1. **Search for Java Repositories**:
   The program uses `searchJavaRepositories` to retrieve a list of Java repositories, limiting to the top 5 based on stars.

2. **Get Latest Commit SHA**:
   For each repository, `getLatestCommitSHA` retrieves the SHA for the latest commit on the repository’s default branch.

3. **Retrieve and Process Java Files**:
   Using `getJavaFiles`, the program fetches the `.java` files for each repository. Each file’s content is analyzed by `getClassNamesFromJavaFile` to extract class names.

4. **Calculate Word Popularity**:
   After extracting class names, `calculateWordPopularity` breaks them down into individual words and calculates the frequency of each word, then displays the 20 most common words.

### Example Output
After running the program, you’ll see output similar to:

```
Repository: iluwatar/java-design-patterns
Java File: patterns/adapter/AdapterPattern.java
Class: AdapterPattern
...
Word Popularity Score (Top 20):
manager: 85
data: 76
controller: 62
list: 60
service: 45
factory: 40
...
```

This output displays the top 20 most common words and their frequencies, based on the first 10,000 class names collected.

### Notes
- **Rate Limits**: If making many requests in a short time, be mindful of GitHub’s rate limits. Using a personal access token increases your rate limits.
- **Default Branch Handling**: The program dynamically adapts to each repository’s default branch, which prevents errors if the default branch isn’t named `main`.

## License
This project is licensed under the MIT License. See the LICENSE file for details.
