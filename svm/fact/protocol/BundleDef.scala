package fact.protocol
import scala.collection.mutable
import spinal.core.Bundle

trait Protocol 

object Sideband extends Protocol 
object FlowBundle extends Protocol
object StreamBundle extends Protocol

trait BundleDir
object Slave extends BundleDir
object Master extends BundleDir

case class BundleDef (
  var protocol: Protocol = null,
  val payload: mutable.HashSet[Wire] = mutable.HashSet[Wire](),
  var dir: BundleDir = null,
  var name: String = "null"
){}
