package svm 

import svm.base._
import svm.tlm.SvmAnalysisPort

class SvmMonitor[T<:SvmObject] extends SvmComponent {
    val ap = new SvmAnalysisPort[T]()
}