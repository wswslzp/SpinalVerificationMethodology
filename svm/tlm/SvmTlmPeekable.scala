package svm.tlm
import svm.base.SvmObject
import svm.logger

trait SvmTlmPeekable[REQ <: SvmObject, RSP <: SvmObject] extends SvmTlmPortBase[REQ, RSP] {
    private val postPeekTasks = scala.collection.mutable.ArrayBuffer.empty[RSP => Unit]
    private var peekTask: Option[() => RSP] = None
    private var tryPeekTask: Option[() => Option[RSP]] = None
    private var canPeekTask: Option[() => Boolean] = None

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
    def whenPeek(task: => RSP): Unit = { peekTask = Some(() => task) }
    
    /**
      * Terminator implement the `canPeek()` method by calling `implCanPeek {...}`
      * @param impl actual implement body of `canPeek()`
      */
    def implCanPeek(impl: => Boolean): Unit = canPeekTask = Some(() => impl)

    def implTryPeek(impl: => Option[RSP]): Unit = tryPeekTask = Some(() => impl)

    /**
      * Initiator call `canPeek()` to determine whether there's response item can get.
      * Non-blocking method, should not consume sim time.
      * @return true if there's item available to get. false if there's no item available or no canPeek() method implementation.
      */
    def canPeek(): Boolean = canPeekTask match {
        case Some(implCanPeekTask) => implCanPeekTask()
        case None => 
            logger.error(f"No canPeek() method implementation")
            false
    }
    
    /**
      * Initiator call `tryPeek()` trying to get one response item.
      * Non-blocking method, should not consume sim time.
      * @return
      */
    def tryPeek(): Option[RSP] = tryPeekTask match {
        case Some(implTryPeekTask) => 
            val ret = implTryPeekTask()
            ret.foreach(rsp => postPeekTasks.foreach(task => task(rsp)))
            ret
        case None => 
            logger.warn(f"No tryPeek() method implementation")
            None
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