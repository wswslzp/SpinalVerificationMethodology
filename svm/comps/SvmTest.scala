package svm.comps

import svm.base.SvmComponent
import spinal.core.Component
import svm.base.SvmRunTest

class SvmTest() extends SvmComponent {
    def run[T <: Component](dut: T): Unit = SvmRunTest(dut, this)
}