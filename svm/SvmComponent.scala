package svm
import spinal.core._
import scala.collection.mutable.ArrayBuffer

class SvmComponent(override val name: String, val parent: SvmComponent) extends SvmObject(name) with SvmPhaseRunnerContainer{
    val thisComponent: SvmComponent with SvmPhaseRunnerContainer = this
    val children = ArrayBuffer.empty[SvmComponent]

    def getFullName(): String = {
        if (parent == null) {
            name
        } else {
            parent.getFullName() + "." + name
        }
    }
    
    def printTopology(): Unit = {
        println(this.getFullName())
        children.foreach(c => println(c.getFullName()))
    }
    
    if (parent != null) parent.children.addOne(this)
}