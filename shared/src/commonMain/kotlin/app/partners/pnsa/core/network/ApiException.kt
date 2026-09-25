package app.partners.pnsa.core.network

class ApiException(
    val statusCode: Int,
    override val message: String,
    val fieldErrors: Map<String, List<String>> = emptyMap(),
    val unauthorized: Boolean = statusCode == 401,
) : Exception(message) {
    val firstFieldError: String?
        get() = fieldErrors.values.flatten().firstOrNull()

    fun userMessage(): String = firstFieldError ?: message
}
