package by.vkatz.example.services

import by.vkatz.leviathan.Leviathan
import java.util.*

//---- services ------

class Api {
    fun getData() = listOf(1, 2, 3, 4, 5)
}

class Db {
    fun getData() = listOf("one", "two", "three", "four", "five")
}

class External {
    private val uuid = UUID.randomUUID().toString()

    fun getId() = uuid
}

interface Data {
    val db: Db
    val api: Api
    val externalStatic: External
    fun getApiData(): List<Int>
    fun getDbData(): List<String>
    fun getExternalServiceIdStatic(): String
    fun getExternalServiceIdDynamic(): String
}

class DataImpl(
    override val db: Db,
    override val api: Api,
    override val externalStatic: External,
    val externalDynamic: () -> External
) : Data {
    override fun getApiData(): List<Int> = api.getData()
    override fun getDbData(): List<String> = db.getData()
    override fun getExternalServiceIdStatic() = externalStatic.getId()
    override fun getExternalServiceIdDynamic() = externalDynamic().getId()
}

//---- services provider ----

object Services : Leviathan() {
    val db by instance { Db() }
    val api by instance { Api() }
    val external by factory { External() }
    val data by instance<Data> { //provide as Data via DataImpl
        DataImpl(
            db = db,
            api = api,
            externalStatic = external,
            externalDynamic = { external }
        )
    }
}