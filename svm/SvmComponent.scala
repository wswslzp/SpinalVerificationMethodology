package svm
import spinal.core._
import scala.collection.mutable.ArrayBuffer

class SvmComponent(name: String = "null", parent: SvmComponent = SvmComponent.svm_root) extends SvmObject(name) { 
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
    
    
    def buildPhase(phase: SvmPhase): Unit = {
        println(f"${getFullName()} entering ${phase.getPhaseName}")
    }
    def runPhase(phase: SvmPhase): Unit = {
        println(f"${getFullName()} entering ${phase.getPhaseName}")
    }
    def checkPhase(phase: SvmPhase): Unit = {
        println(f"${getFullName()} entering ${phase.getPhaseName}")
    }
    
    private def register(): Unit = {
        if (parent != null) parent.children.addOne(this)
        SvmPhaseManager.phaseBuild.addOneTask(this)(buildPhase)
        SvmPhaseManager.phaseRun.addOneTask(this)(runPhase)
        SvmPhaseManager.phaseCheck.addOneTask(this)(checkPhase)
    }
    
    register()
}

object SvmComponent {
    val svm_root: SvmComponent = new SvmComponent("svm_root", null)
    def getTopSvc: SvmComponent = svm_root.children.head
}