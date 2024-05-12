package svm.seq
import svm.base._

abstract class SvmSequence[T<:SvmObject](name: String) extends SvmSequenceItem(name) {
    def start(sqr: SvmSequencer[T]): Unit
    def body(): Unit
}