package svm.comps

import svm.base._
import svm.tlm._

class SvmDriver[T <: SvmObject] extends SvmComponent {
    val exp = !new SvmAnalysisExport[T]()
    val fifo = !new SvmAnalysisFifo[T]()
}