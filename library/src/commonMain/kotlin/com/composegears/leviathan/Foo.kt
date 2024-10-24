import com.composegears.leviathan.Dependency
import com.composegears.leviathan.Leviathan

private interface iDI {
    val dep1: Dependency<String>
    val dep2: Dependency<Int?>
}

private object DI : Leviathan(), iDI {
    override val dep1 by instanceOf { "1" }
    override val dep2 by factoryOf<Int?> { 2 }
}

private object DI2 : Leviathan(), iDI by DI {
    val dep3 = instanceOf { 0f }
    val dep4: Dependency<String> = DI.dep1
    //val dep5: Dependency<String> by DI.dep1
}

private fun f1() {
    val dep11: String = DI2.dep1.get() // String
    val dep13: String by DI2.dep1     // String
    val dep21 = DI2.dep2.get()
    //val dep31 = DI2.dep3.get()
    val dep41 = DI2.dep4.get()


    // testing

    DI.dep1.overrideWith { "2" }
}

private class ViewModel(val di: iDI = DI) {
    val t: String by DI.dep1
    fun run() {}
}


private fun tets() {
    DI.dep1.overrideWith { "1" }

    ViewModel(object : Leviathan(), iDI by DI {
        override val dep2: Dependency<Int?> by instanceOf { 2 }
    })
}
