package com.aditya.stockmarketapp.util

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null,
    val isLoading: Boolean = false
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(isLoading: Boolean = true) : Resource<T>(isLoading = isLoading)
} 