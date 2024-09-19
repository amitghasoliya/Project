package com.amitghasoliya.project

data class ApiResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

data class Message(
    val content: String
)

//data class Content(
//    val titles: List<String>,
//    val description: String
//)