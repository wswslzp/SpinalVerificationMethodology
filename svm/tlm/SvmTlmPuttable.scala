package svm.tlm
import svm.base.SvmObject

trait SvmTlmPuttable[REQ <: SvmObject, RSP <: SvmObject] extends SvmTlmPortBase[REQ, RSP]{
    protected val prePutTasks   = scala.collection.mutable.ArrayBuffer.empty[REQ => Unit]
    protected val postPutTasks  = scala.collection.mutable.ArrayBuffer.empty[REQ => Unit]

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
      * Producer method put(req)
      * Should be called from initiator `SvmTlmPort`. put(req) method calling will be transfer to the downstream export. The transfer is chained and will end up transferring to the terminator SvmTlmExport and the terminator SVC will implement hook `afterPut` and `beforePut` to implement the actual put operation.
      * @param trans request item, sending downward
      */
    def put(trans: REQ): Unit = {
        prePutTasks.foreach(task => task(trans))
        targetExport.foreach(exp => exp.put(trans))
        postPutTasks.foreach(task => task(trans))
    }
}