package com.arsinde.oldboobstore.core.network

import com.arsinde.oldboobstore.core.network.api.BookApi
import com.arsinde.oldboobstore.core.network.api.KtorBookApi
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val BASE_URL = "https://example.test"

private const val VALID_BODY =
    """{"docs":[{"id":"1","title":"Dune","authors":["Frank Herbert"],"cover_image":"http://x/c.jpg","description":"d"}]}"""

/**
 * Builds a [BookApi] over a Ktor mock engine, so no real network is involved.
 * Mirrors the project's real client configuration without duplicating its
 * engine choice.
 */
private fun apiWith(engine: MockEngine): BookApi = KtorBookApi(
    client = HttpClient(engine) {
        expectSuccess = false
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; explicitNulls = false })
        }
    },
    baseUrl = BASE_URL,
)

private fun jsonEngine(
    body: String = VALID_BODY,
    status: HttpStatusCode = HttpStatusCode.OK,
) = MockEngine {
    respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
}

class BookApiResultTest {

    @Test
    fun `successful call maps the payload onto domain types`() = runTest {
        val result = apiWith(jsonEngine()).getBooks()

        val success = result as NetworkResult.Success
        val book = success.data.single()
        assertEquals("1", book.id)
        assertEquals("Dune", book.title)
        assertEquals("Frank Herbert", book.author)
        assertEquals("http://x/c.jpg", book.coverImageUrl)
    }

    @Test
    fun `a failing status surfaces as a typed failure, not an exception`() = runTest {
        val engine = MockEngine { respondError(HttpStatusCode.InternalServerError) }

        val result = apiWith(engine).getBooks()

        assertTrue(result is NetworkResult.ConnectivityFailure)
    }

    @Test
    fun `a malformed payload surfaces as a malformed response failure`() = runTest {
        val result = apiWith(jsonEngine(body = "{not json")).getBooks()

        assertTrue(result is NetworkResult.MalformedResponse)
    }

    @Test
    fun `unknown payload fields are ignored`() = runTest {
        val body = """{"docs":[{"id":"1","title":"Dune","unexpected":123}],"extra":true}"""

        val result = apiWith(jsonEngine(body = body)).getBooks()

        assertEquals("Dune", (result as NetworkResult.Success).data.single().title)
    }

    @Test
    fun `multiple authors are joined into the domain author field`() = runTest {
        val body = """{"docs":[{"id":"7","title":"Dune","authors":["Frank Herbert","Brian Herbert"]}]}"""

        val result = apiWith(jsonEngine(body = body)).getBooks()

        assertEquals(
            "Frank Herbert, Brian Herbert",
            (result as NetworkResult.Success).data.single().author,
        )
    }

    @Test
    fun `a missing optional field maps to null rather than failing`() = runTest {
        val body = """{"docs":[{"id":"9","title":"Solo"}]}"""

        val result = apiWith(jsonEngine(body = body)).getBooks()

        val book = (result as NetworkResult.Success).data.single()
        assertEquals(null, book.coverImageUrl)
        assertEquals("", book.author)
    }
}