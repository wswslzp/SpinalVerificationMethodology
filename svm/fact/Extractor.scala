package fact
import scala.reflect.runtime.universe._
import spinal.core._
import spinal.lib.{Stream, Flow, DataCarrier}
import spinal.lib.tools.ModuleAnalyzer
import fact.protocol._


class Extractor(module: Module) {
  val moduleMirror = runtimeMirror(module.getClass().getClassLoader()).reflect(module)
  val typeSignature = moduleMirror.symbol.typeSignature
  val moduleMembers = typeSignature.members
  var ioBundle: Bundle = module.getClass().getMethod("io").invoke(module).asInstanceOf[Bundle]

  def extractPayload(pd: Data): Set[Wire] = {
    val ret = Set.newBuilder[Wire]
    pd match {
      case md: MultiData => 
        md.elements.map(_._2).foreach({d=>
          val w = Wire(
            name = d.getName(),
            bitWidth = d.getBitsWidth,
            direction = if(d.isInput) Input else Output
          )
          ret += w
        })
      
      case _=> 
        ret += Wire(
          name = pd.getName(),
          bitWidth = pd.getBitsWidth,
          direction = if(pd.isInput) Input else Output
        )
    }
    ret.result()
  }

  def extractBundle(bundle: Bundle): Set[BundleDef] = {
    val ret = Set.newBuilder[BundleDef]
    bundle.elements.map(_._2).foreach({e=>
      e match {
        case s: Stream[_] => 
          val bundle = BundleDef( protocol = StreamBundle)
          bundle.payload ++= extractPayload(s.payload.asInstanceOf[Data])
          bundle.dir = if(s.valid.isInput) Slave else Master
          bundle.name = s.getName()
          ret += bundle
        case f: Flow[_] => 
          val bundle = BundleDef( protocol = FlowBundle)
          bundle.payload ++= extractPayload(f.payload.asInstanceOf[Data])
          bundle.dir = if(f.valid.isInput) Slave else Master
          bundle.name = f.getName()
          ret += bundle
        case bt: BaseType => 
          val bundle = BundleDef( protocol = Sideband)
          bundle.payload += Wire(
            name = bt.getName(),
            bitWidth = bt.getBitsWidth,
            direction = if (bt.isInput) Input else Output
          )
          bundle.dir = if(bt.isInput) Slave else Master
          bundle.name = bt.getName()
          ret += bundle
        case bd: Bundle => 
          ret ++= extractBundle(bundle)
      }
    })
    ret.result()
  }

  /**
    * only extract the io from io bundle
    *
    * @return all the bundle definition
    */
  def extractAllIo(): Set[BundleDef] = {
    val ret = Set.newBuilder[BundleDef]
    ret ++= extractBundle(ioBundle)
    ret.result()
  }
  
  def extractStream(): Set[BundleDef] = {
    val ret = Set.newBuilder[BundleDef]
    moduleMembers.filter(_.isTerm).foreach({member=>
      val memberType = member.typeSignatureIn(typeSignature)
      if (memberType =:= typeOf[Stream[_]]) {
        val realBundle: Stream[_] = moduleMirror.reflectField(member.asTerm).get.asInstanceOf[Stream[_]]
        val bundle = new BundleDef()
        bundle.protocol = StreamBundle
        bundle.payload ++= extractPayload(realBundle.payload.asInstanceOf[Data])
        ret += bundle
      }
    })
    ret.result()
  }

  def extractFlow(): Set[BundleDef] = {
    val ret = Set.newBuilder[BundleDef]
    moduleMembers.filter(_.isTerm).foreach({member=>
      val memberType = member.typeSignatureIn(typeSignature)
      if (memberType =:= typeOf[Flow[_]]) {
        val realBundle: Flow[_] = moduleMirror.reflectField(member.asTerm).get.asInstanceOf[Flow[_]]
        val bundle = new BundleDef()
        bundle.protocol = FlowBundle
        bundle.payload ++= extractPayload(realBundle.payload.asInstanceOf[Data])
        ret += bundle
      }
    })
    ret.result()
  }

  def extractSideband(): Set[BundleDef] = {
    val ret = Set.newBuilder[BundleDef]
    // todo :
    ret.result()
  }

}