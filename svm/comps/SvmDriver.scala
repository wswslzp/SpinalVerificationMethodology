package svm.comps

import svm.base._
import svm.tlm._

class SvmDriver[T <: SvmObject](name: String, parent: SvmComponent) extends SvmComponent(name, parent) {
    val exp = new SvmAnalysisExport[T](f"${getFullName()}.exp")
    val fifo = new SvmAnalysisFifo[T](f"${getFullName()}.fifo")
}