package com.appdev.eudemonia

import com.google.gson.annotations.SerializedName

// OpenAIModels.kt

data class HuggingFaceRequest(
    val inputs: String
)

data class HuggingFaceResponse(
    @SerializedName("generated_text")
    val generatedText: String
)