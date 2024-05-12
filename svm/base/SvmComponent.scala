package svm.base
import svm._
import spinal.core._
import scala.collection.mutable.ArrayBuffer
import svm.svmError
import svm.svmFatal

class SvmComponent extends SvmObject { 
    var parent: SvmComponent = null
    var children = scala.collection.mutable.LinkedHashSet.empty[SvmComponent]
    private var registered = false

    override def getFullName(): String = {
        if (parent == null) {
            getTypeName()
        } else {
            parent.getFullName() + "." + getName()
        }
    }
    
    def printTopology(): Unit = {
        println(this.getFullName())
        children.foreach(c => c.printTopology())
    }
    
    
    def buildPhase(phase: SvmPhase): Unit = {
        svmLow(f"${getFullName()} entering ${phase.getPhaseName}")
    }
    def connectPhase(phase: SvmPhase): Unit = {
        svmLow(f"${getFullName()} entering ${phase.getPhaseName}")
    }
    def runPhase(phase: SvmPhase): Unit = {
        svmLow(f"${getFullName()} entering ${phase.getPhaseName}")
    }
    def checkPhase(phase: SvmPhase): Unit = {
        svmLow(f"${getFullName()} entering ${phase.getPhaseName}")
    }
    
    def register(): Unit = {
        if (!registered) {
            if (parent == null) {
                parent = SvmRoot
                SvmRoot.children += this
            }
            SvmPhaseManager.phaseBuild.addOneTask(this)(buildPhase)
            SvmPhaseManager.phaseConnect.addOneTask(this)(connectPhase)
            SvmPhaseManager.phaseRun.addOneTask(this)(runPhase)
            SvmPhaseManager.phaseCheck.addOneTask(this)(checkPhase)
            registered = true
        }
    }
    
    
    override def valCallback[T](ref: T, name: String): T = {
        ref match {
            case comp: SvmComponent => // All sub SVC
                comp.parent = this
                comp.setName(name)
                if (this.children == null) children = scala.collection.mutable.LinkedHashSet.empty[SvmComponent]
                this.children.addOne(comp)
                comp.register()
            case obj: SvmObject => obj.setName(name) // All other svm objects
            case _ => {}
        }
        ref
    }
}

object SvmRoot extends SvmComponent {
    parent = null
    name = "svm_root"
    override def register(): Unit = {}
    override def getFullName(): String = "SvmRoot"
}

object SvmComponent {
    def getTopSvc: scala.collection.mutable.LinkedHashSet[SvmComponent] = {
        val tops = SvmRoot.children
        tops.foreach(top => top.setName(top.getTypeName()))
        tops
    }
}