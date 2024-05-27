package svm

import svm.base._
import svm.tlm.SvmAnalysisExport
import svm.tlm.SvmAnalysisFifo
import svm.tlm.SvmAnalysisPort

class SvmSubscriber[T<:SvmObject] extends SvmComponent {
    val exp = !new SvmAnalysisExport[T]()
    val fifo = !new SvmAnalysisFifo[T]()
    val ap = !new SvmAnalysisPort[T]()
}