package svm 

import svm.base._
import svm.tlm.SvmAnalysisPort

class SvmMonitor[T<:SvmObject](name: String, parent: SvmComponent) extends SvmComponent(name, parent) {
    val ap = new SvmAnalysisPort[T](f"${getFullName()}.ap")
}