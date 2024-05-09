import svm.SvmComponent

class A() extends SvmComponent("A", null) {
    val A_a = new SvmComponent("a", this)
    val A_b = new SvmComponent("b", this)
    val A_c = new SvmComponent("c", this)
}

object SvmComponentTest extends App {
    val a = new A()
    a.printTopology()
}