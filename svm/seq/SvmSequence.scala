package svm.seq
import svm.base._

abstract class SvmSequence[T<:SvmObject] extends SvmSequenceItem {
    private var sequencer: SvmSequencer[T] = null

    def start(sqr: SvmSequencer[T]): Unit
    def body(): Unit
    
    def setSequencer(seqr: SvmSequencer[T]): Unit = sequencer = seqr
    def getSequencer(seqr: SvmSequencer[T]): SvmSequencer[T] = sequencer

}