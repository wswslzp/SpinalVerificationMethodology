package svm.seq
import svm.base._

abstract class SvmSequence[T<:SvmObject] extends SvmSequenceItem {
    def start(sqr: SvmSequencer[T]): Unit
    def body(): Unit
}