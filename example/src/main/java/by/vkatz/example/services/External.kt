package by.vkatz.example.services

import java.util.*

class External {
    private val uuid = UUID.randomUUID().toString()

    fun getName() = "ExternalService"
    fun getId() = uuid
}