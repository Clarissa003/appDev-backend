// OpenAIModels.kt

data class CompletionRequest(
    val prompt: String,
    val max_tokens: Int = 150
)

data class CompletionResponse(
    val choices: List<CompletionChoice>
)

data class CompletionChoice(
    val text: String
)
