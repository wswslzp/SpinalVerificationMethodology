package svm
import scala.collection.mutable.HashMap

object SvmFactory {
    val objectRegistry = HashMap.empty[Int, String => SvmObject]
    val componentRegistry = HashMap.empty[Int, (String, SvmComponent) => SvmComponent]
}