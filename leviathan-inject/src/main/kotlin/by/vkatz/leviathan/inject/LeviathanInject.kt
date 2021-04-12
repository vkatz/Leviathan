package by.vkatz.leviathan.inject


inline fun <reified T> inject(serviceName: String = LeviathanService.LEVIATHAN_DEFAULT_SERVICE_NAME): T {
    //todo generate smth to get deps
    return null as T // yolo
}


/*
//gen idea
fun get(c: KClass<Int>): Int = 1
fun get(c: KClass<String>): String = "asd"
fun get(c: KClass<Unit>): Unit = Unit

fun boo() {
    val t1: Int = get(Int::class)
    val t2: String = get(String::class)
    val t3: Unit = get(Unit::class)
}
*/