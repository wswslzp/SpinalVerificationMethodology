package svm.tlm
import svm.base.SvmObject
import svm.logger
import spinal.core.sim._

trait SvmTlmGettable[REQ <: SvmObject, RSP <: SvmObject] extends SvmTlmPortBase[REQ, RSP] {
    private val postGetTasks  = scala.collection.mutable.ArrayBuffer.empty[RSP => Unit]
    private var getTask: Option[() => RSP] = None
    private var tryGetTask: Option[() => Option[RSP]] = None
    private var canGetTask: Option[() => Boolean] = None

    /**
      * Hook function.
      * Will be called after actual get() method being invoked. 
      * @param task takes a response item as param. It's intended to do something on the response item as post-process.
      */
    def afterGet(task: RSP => Unit): Unit = postGetTasks.addOne(task)
    
    /**
      * When the initiator TLM port call get() method, the terminator export should call a task that returns a response item. 
      * this task is defined by `whenGet` method. 
      * @param task This task should take simulation time as it's blocking get. 
      */
    def whenGet(task: => RSP): Unit = { getTask = Some(() => task) }
    
    /**
      * Terminator implement the `canGet()` method by calling `implCanGet {...}`
      * @param impl actual implement body of `canGet()`
      */
    def implCanGet(impl: => Boolean): Unit = canGetTask = Some(() => impl)

    def implTryGet(impl: => Option[RSP]): Unit = tryGetTask = Some(() => impl)

    /**
      * Initiator call `canGet()` to determine whether there's response item can get.
      * Non-blocking method, should not consume sim time.
      * @return true if there's item available to get. false if there's no item available or no canGet() method implementation.
      */
    def canGet(): Boolean = canGetTask match {
        case Some(implCanGetTask) => implCanGetTask()
        case None => 
            logger.error(f"No canGet() method implementation")
            false
    }
    
    /**
      * Initiator call `tryGet()` trying to get one response item.
      * Non-blocking method, should not consume sim time.
      * @return
      */
    def tryGet(): Option[RSP] = tryGetTask match {
        case Some(implTryGetTask) => 
            val ret = implTryGetTask()
            ret.foreach(rsp => postGetTasks.foreach(task => task(rsp)))
            ret
        case None => 
            logger.warn(f"No tryGet() method implementation")
            None
    }
        
    /**
      * Initiator call `get()` method to get the response item
      * The terminator componenet should implement the `get()` method by calling `whenGet { ... }`
      * @return response item
      */
    def get(): RSP = {
        val ret = getTask.map(task => task())
        ret match {
            case Some(rsp) => 
                if (targetExport != None) logger.warn(f"There's target export defined but also get() task defined. Will use get() method instead.")
                postGetTasks.foreach(task => task(rsp))
                rsp
            case None => 
                targetExport match {
                    case Some(exp) => exp.get()
                    case None => 
                        logger.error(f"No downstream exp/imp port defined and no get method implementation, error")
                        throw new RuntimeException
                }
        }
    }
}