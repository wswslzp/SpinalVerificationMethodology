package svm.seq

/**
  * Should have one method: item_done(). 
  * 
  * `item_done` method should notify the parent sequence that this item transaction is done. 
  * Then sequence call finish_item to finish one item transaction. 
  */
abstract class SvmSequenceItem extends SvmTransaction {
    var parent_sequence: Option[SvmSequence[SvmSequenceItem]] = None
    
    def item_done(): Unit = {
        parent_sequence.foreach(seq => seq.state = SSS_IDLE)
    }
}