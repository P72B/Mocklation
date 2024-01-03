package de.p72b.mocklation.data.util

data class Resource<out T>(
    val status: Status,
    val data: T? = null,
    val message: String? = null,
    val code: Int? = null
) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, msg)
        }
    }
}