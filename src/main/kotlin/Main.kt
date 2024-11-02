import kotlinx.serialization.*
import kotlinx.serialization.json.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.Base64

const val GITHUB_TOKEN = "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" // Replace with your GitHub token

// Data classes for JSON serialization
@Serializable
data class Repo(
    val full_name: String,
    val default_branch: String // Add default_branch here
)

@Serializable
data class SearchResponse(val items: List<Repo>)

@Serializable
data class TreeNode(val path: String, val type: String, val sha: String)

@Serializable
data class TreeResponse(val tree: List<TreeNode>)

@Serializable
data class BlobResponse(val content: String)

val client = OkHttpClient()
val json = Json { ignoreUnknownKeys = true }

val classNamesList = mutableListOf<String>() // Stores class names
val base64Regex = Regex("^[A-Za-z0-9+/=]+\$")

fun searchJavaRepositories(): List<Repo> {
    val request = Request.Builder()
        .url("https://api.github.com/search/repositories?q=language:Java&sort=stars&order=desc&per_page=5")
        .header("Authorization", "Bearer $GITHUB_TOKEN")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        val responseBody = response.body?.string() ?: return emptyList()
        val searchResponse = json.decodeFromString<SearchResponse>(responseBody)
        return searchResponse.items
    }
}

fun getLatestCommitSHA(repoName: String, defaultBranch: String): String {
    val request = Request.Builder()
        .url("https://api.github.com/repos/$repoName/branches/$defaultBranch")
        .header("Authorization", "Bearer $GITHUB_TOKEN")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        val responseBody = response.body?.string() ?: throw IOException("Empty response body")
        val jsonObject = json.parseToJsonElement(responseBody).jsonObject
        return jsonObject["commit"]?.jsonObject?.get("sha")?.jsonPrimitive?.content
            ?: throw IOException("Commit SHA not found")
    }
}

fun getJavaFiles(repoName: String, commitSHA: String): List<TreeNode> {
    val request = Request.Builder()
        .url("https://api.github.com/repos/$repoName/git/trees/$commitSHA?recursive=1")
        .header("Authorization", "Bearer $GITHUB_TOKEN")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        val responseBody = response.body?.string() ?: throw IOException("Empty response body")
        val treeResponse = json.decodeFromString<TreeResponse>(responseBody)
        return treeResponse.tree.filter { it.path.endsWith(".java") && it.type == "blob" }
    }
}

fun getClassNamesFromJavaFile(repoName: String, fileSHA: String): List<String> {
    val request = Request.Builder()
        .url("https://api.github.com/repos/$repoName/git/blobs/$fileSHA")
        .header("Authorization", "Bearer $GITHUB_TOKEN")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        val responseBody = response.body?.string() ?: throw IOException("Empty response body")
        val blobResponse = json.decodeFromString<BlobResponse>(responseBody)

        val cleanContent = blobResponse.content.replace("\n", "").replace("\r", "")
        val paddedContent = when (cleanContent.length % 4) {
            2 -> "$cleanContent=="
            3 -> "$cleanContent="
            else -> cleanContent
        }

        if (paddedContent.isNotBlank() && paddedContent.matches(base64Regex)) {
            return try {
                val decodedContent = String(Base64.getDecoder().decode(paddedContent))
                val classRegex = Regex("\\bclass\\s+(\\w+)")
                classRegex.findAll(decodedContent).map { it.groupValues[1] }.toList()
            } catch (e: IllegalArgumentException) {
                println("Failed to decode content for file $repoName/$fileSHA: ${e.message}")
                emptyList()
            }
        } else {
            println("Content not valid Base64 for file $repoName/$fileSHA")
            return emptyList()
        }
    }
}

fun calculateWordPopularity(classNames: List<String>): Map<String, Int> {
    val wordFrequency = mutableMapOf<String, Int>()

    for (className in classNames) {
        // Split the class name into words (assuming CamelCase convention)
        val words = className.split("(?<!^)(?=[A-Z])".toRegex())
        for (word in words) {
            wordFrequency[word.toLowerCase()] = wordFrequency.getOrDefault(word.toLowerCase(), 0) + 1
        }
    }

    return wordFrequency
}

fun main() {
    val repos = searchJavaRepositories()
    for (repo in repos) {
        println("Repository: ${repo.full_name}")

        // Use the repository's default branch
        val commitSHA = getLatestCommitSHA(repo.full_name, repo.default_branch)
        val javaFiles = getJavaFiles(repo.full_name, commitSHA)

        for (file in javaFiles) {
            println("Java File: ${file.path}")

            // Retrieve and store class names
            val classNames = getClassNamesFromJavaFile(repo.full_name, file.sha)
            classNamesList.addAll(classNames)

            // Stop if we reach 10,000 class names
            if (classNamesList.size >= 2000) break
        }
        if (classNamesList.size >= 2000) break
    }

    // Calculate and print word popularity
    val wordPopularity = calculateWordPopularity(classNamesList.take(10000))
    println("Word Popularity Score (Top 20):")
    wordPopularity.entries.sortedByDescending { it.value }.take(20).forEach { (word, score) ->
        println("$word: $score")
    }
}
