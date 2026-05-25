package com.ai.food.recognition.data

object SessionManager {

    const val BASE_URL = "http://192.168.1.2:3000/"

    var email: String? = null
    var password: String? = null
    var fullName: String? = null
    var avatarUri: String? = null

    var accessToken: String? = null
    var refreshToken: String? = null

    var carbs: Int = 0
    var proteins: Int = 0
    var fats: Int = 0
    var calories: Int = 0

    var consumedCalories: Int = 0
    var consumedCarbs: Int = 0
    var consumedProteins: Int = 0
    var consumedFats: Int = 0
}