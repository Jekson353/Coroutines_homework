package ru.samoylenko.coroutines.dto

data class PostWithComments (
    val post: Post,
    val comment: List<Comment>,
)