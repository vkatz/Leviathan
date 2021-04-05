package by.vkatz.example.services

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