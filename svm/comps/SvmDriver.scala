package svm.comps

import svm.base._
import svm.tlm._

/**
  * In run phase, driver call seq_item_port.get() to get a sequence item.
  * req = seq_item_port.get() 
  * 
  * then use req.item_done() to notify upstream sequencer that current sequence item transaction is done.
  */
class SvmDriver[T <: SvmObject] extends SvmComponent {
    val exp = !new SvmAnalysisExport[T]()
    val fifo = !new SvmAnalysisFifo[T]()
    
    val seq_item_port = !new SvmTlmGetPort[T, T]()
}