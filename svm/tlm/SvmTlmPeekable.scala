package svm.tlm
import svm.base.SvmObject
import svm.logger

trait SvmTlmPeekable[REQ <: SvmObject, RSP <: SvmObject] extends SvmTlmPortBase[REQ, RSP] {
    private val postPeekTasks = scala.collection.mutable.ArrayBuffer.empty[RSP => Unit]
    private var peekTask: Option[() => RSP] = None

    /**
      * Hook function.
      * Will be called after actual peek() method being invoked. 
      * @param task takes a response item as param. It's intended to do something on the response item as post-process.
      */
    def afterPeek(task: RSP => Unit): Unit = postPeekTasks.addOne(task)
    
    /**
      * When the initiator TLM port call peek() method, the terminator export should call a task that returns a response item.
      * this task is defined by `whenPeek` method. 
      * @param task This task should take simulation time as it's blocking peek. 
      */
    def whenPeek(task: => RSP): Unit = {
        peekTask = Some(() => task)
    }

    // Sematic of this task is to get the copy of the current request item but not consume the original one.
    def peek(): RSP = {
        val ret = peekTask.map(task => task())
        ret match {
            case Some(rsp) => 
                if (targetExport != None) logger.warn(f"There's target export defined but also peek() task defined. Will use peek() method instead.")
                postPeekTasks.foreach(task => task(rsp))
                rsp
            case None => 
                targetExport match {
                    case Some(exp) => exp.peek()
                    case None => 
                        logger.error(f"No downstream exp/imp port defined and no peek method implementation, error")
                        throw new RuntimeException
                }
        }
    }
}