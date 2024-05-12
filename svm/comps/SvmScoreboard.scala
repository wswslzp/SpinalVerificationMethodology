package svm 

import svm.base._
import svm.tlm.SvmAnalysisFifo

class SvmScoreboard[T<:SvmObject] extends SvmComponent {
    val actFifo = new SvmAnalysisFifo[T]
    val expFifo = new SvmAnalysisFifo[T]
    private var matchedFunc = (act: T, exp: T) => {}
    private var mismatchedFunc = (act: T, exp: T) => {}
    
    def onMatched(cb: (T, T) => Unit): Unit = {matchedFunc = cb}
    def onMismatched(cb: (T, T) => Unit): Unit = {mismatchedFunc = cb}

    override def runPhase(phase: SvmPhase): Unit = {
        super.runPhase(phase)
        while (true) {
            val act_txn = actFifo.get()
            val exp_txn = expFifo.peek()
            exp_txn match {
                case None => svmError(f"Data no match")
                case Some(value) => 
                    if (!(value.equals(act_txn))) mismatchedFunc(act_txn, value)
                    else matchedFunc(act_txn, value)
            }
        }
    }
}