package svm.seq

import svm.base._
import svm.tlm.SvmAnalysisPort

class SvmSequencer[T <: SvmObject] extends SvmComponent {
    val ap = new SvmAnalysisPort[T]()
    
    def sendItem(it: T): Unit = {
        ap.write(it)
    }
}