package ru.samoylenko.coroutines

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.*
import ru.samoylenko.coroutines.dto.*
import kotlin.coroutines.EmptyCoroutineContext

val client = OkHttpClient.Builder().build()

fun main(){

    CoroutineScope(EmptyCoroutineContext+ Dispatchers.IO).launch {
        try {
            val posts = makeApiCall<List<Post>>("http://localhost:9999/api/posts").map {post ->
                async {
                    val author = makeApiCall<Author>("http://localhost:9999/api/authors/${post.authorId}")
                    PostWithAuthor(post, author)
                }
            }.awaitAll()
            println(posts)
        }catch (e: Exception){
            println("При выполнении запроса произошла неизвестная ошибка")
        }
    }

Thread.sleep(40_000)
}

suspend inline fun <reified T> makeApiCall(url: String, gson: Gson = Gson()): T =
    withContext(Dispatchers.IO) {
        println(url)
        Request.Builder()
            .url(url)
            .build()
            .let {
                client.newCall(it)
            }
            .execute()
            .body
            ?.string()
            .let {
                requireNotNull(it){
                    "Body is null"
                }
            }.let {
                gson.fromJson(it, object : TypeToken<T>(){}.type)
            }
    }
