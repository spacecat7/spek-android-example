package org.spekframework.speksample


data class UserDto(val name: String)

fun UserDto.toDomain() : User {
    return User(this.name)
}

data class User(val name: String)

class Repository constructor(private val apiClient: ApiClient) {

    suspend fun getUser(id: String): Result<User> {
        return apiClient.getUser(id)
                .awaitResult()
                .map { it.toDomain() }
    }
}