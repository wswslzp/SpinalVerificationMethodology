package svm.tlm
import svm._
import svm.base._

class SvmAnalysisExport[T <: SvmObject] extends SvmObject {
    val downstreams = scala.collection.mutable.ArrayBuffer[SvmAnalysisExport[T]]()
    var beforeWritingTask : T => Unit = null
    var afterWritingTask : T => Unit = null
    def connect(exp: SvmAnalysisExport[T]) : this.type = {
        svmLow(f"${this.getFullName()} connects ${exp.getFullName()}")
        downstreams.addOne(exp)
        this
    }
    def >>(exp: SvmAnalysisExport[T]): this.type = {connect(exp)}
    def beforeWrite(task: T => Unit): this.type = {
        beforeWritingTask = task
        this
    }
    def afterWrite(task: T => Unit): this.type = {
        afterWritingTask = task
        this
    }
    def write(obj: T): Unit = {
        if (beforeWritingTask != null) beforeWritingTask(obj)
        downstreams.foreach(exp => exp.write(obj))
        if (afterWritingTask != null) afterWritingTask(obj)
    }
}