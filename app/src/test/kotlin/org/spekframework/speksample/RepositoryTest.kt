package org.spekframework.speksample

import io.mockk.every
import io.mockk.mockk
import io.mockk.staticMockk
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.Assert
import org.junit.Assert.assertThat
import retrofit2.Call
import org.junit.Assert.assertThat
import org.hamcrest.CoreMatchers.equalTo

object RepositoryTest : Spek({

    // mocks
    val apiClient: ApiClient = mockk()
    val call: Call<UserDto> = mockk()
    val userDto: UserDto = mockk()
    val resultDto: Result<UserDto> = mockk()

    val repository: Repository = Repository(apiClient)
    val expectedResult = User("name")

    on("login"){

        it("should return user object") {

            staticMockk("org.spekframework.speksample.ApiClientKt").use {
                every { apiClient.getUser(any()) } returns call
                every {
                    runBlocking {
                        call.awaitResult()
                    }
                } returns Result.Successful(UserDto("name"))

                every {
                    userDto.toDomain()
                } returns User("name")

                every {
                    resultDto.map { it.toDomain() }
                } returns Result.Successful(User("name"))

                runBlocking {
                    val result: Result<User> = repository.getUser("")
                    if (result is Result.Successful) {
                        assertThat(result.response.name, equalTo(expectedResult.name))
                    }
                }
            }
        }
    }
})
