package svm.seq
import svm.base._
import spinal.core.sim._

trait SvmSequenceState
object SSS_IDLE extends SvmSequenceState
object SSS_STARTED_ITEM extends SvmSequenceState
object SSS_GRANTED_BY_SEQR extends SvmSequenceState

/**
  * Sequence 
  * 
  * Sending sequence item flow:
    1. start one item, by calling `seq.start_item(item)` 
    1. finish one item processing, by calling `seq.finish_item(item)` 
  * 
  * First step waits for the bound sequencer's grant. 
  * After granted, do something process on the `req` item. 
  * `req` will be the one that being retrieve by bound sequencer
  *
  * Second step, waits for the item done. 
  * Item done event is the time when this sequence internal state is set to be SSS_IDLE
  */
abstract class SvmSequence[T<:SvmSequenceItem] extends SvmSequenceItem {
    var sequencer: Option[SvmSequencer[T]] = None
    var priority = 0
    var req: Option[T] = None
    var state: SvmSequenceState = SSS_IDLE
    
    /**
      * Method that begging for downstream sequencer's granted. 
      * Setting internal state to be `SSS_STARTED_ITEM`
      * Return when be granted, means the internal state is set to `SSS_GRANTED_BY_SEQR`
      *
      * @param item sending item, pre-processed one
      */
    def start_item(item: T): Unit = {
        req = Some(item)
        state = SSS_STARTED_ITEM
        waitUntil(state == SSS_GRANTED_BY_SEQR)
    }
    
    /**
      * Method that wait for the item done
      */
    def finish_item(): Unit = {
        waitUntil(state == SSS_IDLE)
    }

    /**
      * Start one sequence
      * 
      * By following steps:
        - set sequencer to be seqr
      *
      * Typical use case. 
      * When in a test run phase, create one sequence object, then call start(p_sequener)
      * @param seqr
      */
    def start(seqr: Option[SvmSequencer[T]] = None): Unit = {
        seqr.foreach(sqr => sequencer = Some(sqr))
        body()
    }
    def body(): Unit
    
    def setSequencer(seqr: SvmSequencer[T]): Unit = {
        sequencer = Some(seqr)
        seqr.bound_sequences.addOne(this)
    }

}