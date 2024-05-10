package svm.tlm
import svm._
import spinal.core.sim._

class SvmAnalysisFifo[T <: SvmObject](name: String) extends SvmObject(name) {
    val queue = scala.collection.mutable.Queue[T]()
    val export = new SvmAnalysisExport[T](f"${name}_exp")
    
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