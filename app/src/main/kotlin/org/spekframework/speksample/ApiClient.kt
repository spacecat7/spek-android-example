package org.spekframework.speksample

import kotlinx.coroutines.experimental.CancellableContinuation
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query


interface ApiClient {

    @POST("/login")
    fun login(@Body email: String) : Call<UserDto>

    @POST("/user")
    fun getUser(@Query("id") id: String) : Call<UserDto>
}

suspend fun <T : Any> Call<T>.awaitResult(): Result<T> {
    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>?, response: Response<T>) {
                continuation.resume(
                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body == null) {
                                Result.Error("error")
                            } else {
                                Result.Successful(body)
                            }
                        } else {
                            Result.Error("error")
                        }
                )
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                // Don't bother with resuming the continuation if it is already cancelled.
                if (continuation.isCancelled) return
                continuation.resume(Result.Error("exception"))
            }
        })

        registerOnCompletion(continuation)
    }
}

private fun Call<*>.registerOnCompletion(continuation: CancellableContinuation<*>) {
    continuation.invokeOnCompletion {
        if (continuation.isCancelled)
            try {
                cancel()
            } catch (ex: Throwable) {
                //Ignore cancel exception
            }
    }
}

sealed class Result<out T : Any> {
    class Successful<out T : Any>(val response: T) : Result<T>() {
        override fun toString() = "Result.Ok{value=$response}"
    }

    class Error(val apiError: String) : Result<Nothing>() {
        override fun toString() = "Result.Error{exception=$apiError}"
    }
}

inline fun <T : Any, R: Any> Result<T>.map(transform: (T) -> R): Result<R> {
    return when(this) {
        is Result.Successful -> Result.Successful(transform(response))
        is Result.Error -> this
    }
}