package svm.seq
import spinal.core.sim._
import svm.base._
import svm.tlm.SvmAnalysisPort
import svm.tlm.SvmTlmGetExport

/**
  * Sequence Arbitor
  */
trait SvmSequenceArbitor {
    def arbitrate[T<:SvmSequenceItem](seqs: Seq[SvmSequence[T]]): SvmSequence[T] 
    def grant[T<:SvmSequenceItem](seqs: Seq[SvmSequence[T]]): SvmSequence[T] = {
        val seq = arbitrate(seqs)
        seq.state = SSS_GRANTED_BY_SEQR
        seq
    }
}
object HIGHEST extends SvmSequenceArbitor {
    override def arbitrate[T <: SvmSequenceItem](seqs: Seq[SvmSequence[T]]): SvmSequence[T] = {
        seqs.maxBy(seq => seq.priority)
    }
}

/**
  * # Sequencer
  *
  * This connects to driver through a seq_item_port. driver.seq_item_port >> seqr.seq_item_export
  * 
  * One sequence can bind one sequencer. This sequencer will then record the bound sequence object. 
  * Every time the driver request a sequence item, this sequencer will poll its bound sequences to get the sequence item.
  * - if no sequences bound, then no sequence item found, and holds the run phase thread.
  * - if found bound sequences, check if they have started items. 
  *     - if no one started, waits until one started.
  *     - if one sequence started, grant this sequence. Take the started item from the sequence. send the item to the downstream driver. 
  *     - if two or more sequences started, grant the highest priority sequence. Take the started item from the sequence. send the item to the downstream driver. 
  *     
  * ## Sequence arbitrator
  * 
  * Use sequence arbitration method to get the highest priority sequence from all bound sequences. .
  * The arbitration method can be pre-defined or user customized. 
  */
class SvmSequencer[T <: SvmSequenceItem] extends SvmComponent {
    val ap = !new SvmAnalysisPort[T]()
    val seq_item_export = !new SvmTlmGetExport[T, T]()
    val bound_sequences = scala.collection.mutable.ArrayBuffer.empty[SvmSequence[T]]
    var arbitor: Option[SvmSequenceArbitor] = None
    
    def setArbitor(arb: SvmSequenceArbitor) = arbitor = Some(arb)
    
    seq_item_export.whenGet {
        val req: Option[T] = None
        waitUntil(bound_sequences.exists(seq => seq.state == SSS_STARTED_ITEM))
        val started_item_seqs = bound_sequences.filter(seq => seq.state == SSS_STARTED_ITEM)
        val seq = arbitor.map(arb => arb.grant(started_item_seqs.toSeq))
        req.get
    }
    
    def sendItem(it: T): Unit = {
        ap.write(it)
    }
}