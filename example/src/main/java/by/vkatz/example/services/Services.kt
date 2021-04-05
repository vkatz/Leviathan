package by.vkatz.example.services

import by.vkatz.leviathan.Leviathan

object Services : Leviathan() {
    val db = instance { Db() }
    val api = instance { Api() }
    val external = newInstance { External() }
    val data = instance<Data> {
        DataImpl(db(),
            api(),
            external(),
            { external() }
        )
    }
}