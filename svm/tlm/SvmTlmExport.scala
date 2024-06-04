package svm.tlm

import svm.logger
import svm.base.SvmObject

// Svm TLM1.0 one-to-one transport 
class SvmTlmExport[REQ <: SvmObject, RSP <: SvmObject] extends SvmPostBase[REQ, RSP] {
    private var targetExport: Option[SvmTlmExport[REQ, RSP]] = None

    private val prePutTasks   = scala.collection.mutable.ArrayBuffer.empty[REQ => Unit]
    private val postPutTasks  = scala.collection.mutable.ArrayBuffer.empty[REQ => Unit]
    private val postGetTasks  = scala.collection.mutable.ArrayBuffer.empty[RSP => Unit]
    private val postPeekTasks = scala.collection.mutable.ArrayBuffer.empty[RSP => Unit]

    private var getTask: Option[() => RSP] = None
    private var peekTask: Option[() => RSP] = None

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
      * Hook function.
      * Will be called after actual get() method being invoked. 
      * @param task takes a response item as param. It's intended to do something on the response item as post-process.
      */
    def afterGet(task: RSP => Unit): Unit = postGetTasks.addOne(task)

    /**
      * Hook function.
      * Will be called after actual peek() method being invoked. 
      * @param task takes a response item as param. It's intended to do something on the response item as post-process.
      */
    def afterPeek(task: RSP => Unit): Unit = postPeekTasks.addOne(task)
    
    /**
      * When the initiator TLM port call get() method, the terminator export should call a task that returns a response item. 
      * this task is defined by `whenGet` method. 
      * @param task This task should or can take simulation time if you defined it as blocking get. Otherwise it's non-blocking get. Task should consume the response item.
      */
    def whenGet(task: => RSP): Unit = {
        getTask = Some(() => task)
    }

    /**
      * When the initiator TLM port call peek() method, the terminator export should call a task that returns a response item.
      * this task is defined by `whenPeek` method. 
      * @param task This task should or can take simulation time if you defined it as blocking peek. Otherwise it's non-blocking peek. Task should not consume the response item.
      */
    def whenPeek(task: => RSP): Unit = {
        peekTask = Some(() => task)
    }

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
    override def connect(exp: SvmPostBase[REQ, RSP]) : this.type = {
        logger.trace(f"${this.getFullName()} connects ${exp.getFullName()}")
        targetExport = Some(exp.asInstanceOf[SvmTlmExport[REQ, RSP]])
        this
    }
}

/**
  * Conceptually, it's the initiator port
  */
class SvmTlmPort[REQ<:SvmObject, RSP<:SvmObject] extends SvmTlmExport[REQ, RSP]

/**
  * Conceptually, it's the terminator port
  */
class SvmTlmImp[REQ<:SvmObject, RSP<:SvmObject] extends SvmTlmExport[REQ, RSP]
