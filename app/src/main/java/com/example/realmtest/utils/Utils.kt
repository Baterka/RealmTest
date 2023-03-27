package com.example.realmtest.utils

import kotlin.random.Random

class Utils {
    companion object {
        fun randomString(): String {
            val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            return (1..5)
                .map { Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("")
        }
    }
}