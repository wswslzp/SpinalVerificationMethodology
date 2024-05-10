import spinal.core._
import spinal.lib._

case class SvmRtlForTest() extends Component {
    val io = new Bundle {
        val a = in Bits(8 bit)
        val b = out Bits(8 bit)
        
        val slv_a = slave Stream(Bits(32 bit))
        val mst_b = master Stream(Bits(32 bit))
    }
    io.b := RegNext(io.a)
    
    io.mst_b <-/< io.slv_a
}