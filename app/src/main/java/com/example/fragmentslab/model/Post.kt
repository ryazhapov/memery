package com.example.fragmentslab.model

data class Post (
    val id: Long,
    val description: String,
    val votes: Long,
    val author: String,
    val date: String,
    val gifURL: String,
    val gifSize: Long,
    val previewURL: String,
    val videoURL: String,
    val videoPath: String,
    val videoSize: Long,
    val type: String,
    val width: String,
    val height: String,
    val commentsCount: Long,
    val fileSize: Long,
    val canVote: Boolean
)