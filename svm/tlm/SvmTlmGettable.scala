package svm.tlm
import svm.base.SvmObject
import svm.logger

trait SvmTlmGettable[REQ <: SvmObject, RSP <: SvmObject] extends SvmTlmPortBase[REQ, RSP] {
    private val postGetTasks  = scala.collection.mutable.ArrayBuffer.empty[RSP => Unit]
    private var getTask: Option[() => RSP] = None

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
    def whenGet(task: => RSP): Unit = {
        getTask = Some(() => task) // TODO: Only blocking now
    }

    // Consumer method
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