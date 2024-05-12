package svm

import svm.base._
import svm.tlm.SvmAnalysisExport
import svm.tlm.SvmAnalysisFifo
import svm.tlm.SvmAnalysisPort

class SvmSubscriber[T<:SvmObject](name: String, parent: SvmComponent) extends SvmComponent(name, parent) {
    val exp = new SvmAnalysisExport[T](f"${getFullName()}.exp")
    val fifo = new SvmAnalysisFifo[T](f"${getFullName()}.fifo")
    val ap = new SvmAnalysisPort[T](f"${getFullName()}.ap")
}