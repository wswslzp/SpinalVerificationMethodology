package svm.tlm
import svm._
import svm.base._
import svm.logger

class SvmAnalysisExport[T <: SvmObject] extends SvmPortBase[T, T] {
    val downstreams = scala.collection.mutable.ArrayBuffer[SvmAnalysisExport[T]]()
    var beforeWritingTask : T => Unit = null
    var afterWritingTask : T => Unit = null
    override def connect(exp: SvmPortBase[T, T]) : this.type = {
        logger.trace(f"${this.getFullName()} connects ${exp.getFullName()}")
        downstreams.addOne(exp.asInstanceOf[SvmAnalysisExport[T]])
        this
    }
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