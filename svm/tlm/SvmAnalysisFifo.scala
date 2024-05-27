package svm.tlm
import svm.base._
import spinal.core.sim._

class SvmAnalysisFifo[T <: SvmObject] extends SvmObject {
    val queue = scala.collection.mutable.Queue[T]()
    val export = !new SvmAnalysisExport[T]()
    
    export afterWrite { obj =>
        queue.enqueue(obj)
    }
    def pop(): T = {
        waitUntil(queue.nonEmpty)
        queue.dequeue()
    }
    def get() : T = pop()
    def peek() : Option[T] = {
        if (queue.isEmpty) {None}
        else {Option(queue.dequeue())}
    }
}