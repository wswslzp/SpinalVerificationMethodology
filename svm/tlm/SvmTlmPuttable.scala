package svm.tlm
import svm.logger
import svm.base.SvmObject
import spinal.sim._
import spinal.core.sim._

trait SvmTlmPuttable[REQ <: SvmObject, RSP <: SvmObject] extends SvmTlmPortBase[REQ, RSP]{
    protected val prePutTasks   = scala.collection.mutable.ArrayBuffer.empty[REQ => Unit]
    protected val postPutTasks  = scala.collection.mutable.ArrayBuffer.empty[REQ => Unit]
    private var tryPutTask: Option[REQ => Boolean] = None
    private var canPutTask: Option[() => Boolean] = None

    /**
      * Hook function.
      * Will be called before actual put() method being invoked.
      * @param task takes a request item as param. It intend to do something on the request item as pre-process.
      */
    def beforePut(task: REQ => Unit): Unit = prePutTasks.addOne(task)

    /**
      * Hook function.
      * Will be called after actual put() method being invoked.
      * @param task takes a request item as param. It intend to do something on the request item as post-process.
      */
    def afterPut(task: REQ => Unit): Unit = postPutTasks.addOne(task)

    /**
      * Terminator implement the `canPut()` method by calling `implCanPut {...}`
      * @param impl actual implement body of `canPut()`
      */
    def implCanPut(impl: => Boolean): Unit = canPutTask = Some(() => impl)

    def implTryPut(impl: REQ => Boolean): Unit = tryPutTask = Some(impl)

    /**
      * Initiator call `canPut()` to determine whether there's response item can get.
      * Non-blocking method, should not consume sim time.
      * @return true if there's item available to get. false if there's no item available or no canPut() method implementation.
      */
    def canPut(): Boolean = canPutTask match {
        case Some(implCanPutTask) => implCanPutTask()
        case None => 
            logger.error(f"No canPut() method implementation")
            false
    }
    
    /**
      * Initiator call `tryPut()` trying to get one response item.
      * Non-blocking method, should not consume sim time.
      * @return
      */
    def tryPut(req: REQ): Boolean = tryPutTask match {
        case Some(implTryPutTask) => 
          val result = canPutTask match {
            case Some(implCanPutTask) => implCanPutTask()
            case None => false
          }
          if (result) {
            val thread = fork {
              prePutTasks.foreach(task => task(req))
              targetExport.foreach(exp => exp.put(req))
              postPutTasks.foreach(task => task(req))
            }
          }
          result
        case None => 
            logger.warn(f"No tryPut() method implementation")
            false
    }
    /**
      * Producer method put(req)
      * Should be called from initiator `SvmTlmPort`. put(req) method calling will be transfer to the downstream export. The transfer is chained and will end up transferring to the terminator SvmTlmExport and the terminator SVC will implement hook `afterPut` and `beforePut` to implement the actual put operation.
      * @param trans request item, sending downward
      */
    def put(req: REQ): Unit = {
        prePutTasks.foreach(task => task(req))
        targetExport.foreach(exp => exp.put(req))
        postPutTasks.foreach(task => task(req))
    }
}