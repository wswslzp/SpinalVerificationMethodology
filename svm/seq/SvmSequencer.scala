package svm.seq

import svm.base._
import svm.tlm.SvmAnalysisPort

class SvmSequencer[T <: SvmObject](name: String, parent: SvmComponent) extends SvmComponent(name, parent) {
    val ap = new SvmAnalysisPort[T](f"${getFullName()}.ap")
}