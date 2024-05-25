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
    
    // Register all the phase method into phase manager
    // If user want to add new phase, this method should be overriden as well.
    def registerPhases(): Unit = {
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
    
    def getClone(): SvmComponent = this.clone().asInstanceOf[SvmComponent]
    
    // Magic here, 
    // iterate all the sub SVC here, from the bottom to top, same order as build_phase. 
    // This method is to build the whole SVM component tree, 
    // and set the name of the SVC instance identical to the variable name.
    // This method enable user not to have to set name/parent by hand, but automatically by SVM itself.
    override def valCallback[T](ref: T, name: String): T = {
        ref match {
            case comp: SvmComponent => 
                comp.parent = this
                comp.setName(name)
                if (this.children == null) children = scala.collection.mutable.LinkedHashSet.empty[SvmComponent]
                this.children.addOne(comp)
                comp.registerPhases()
            case obj: SvmObject => obj.setName(name) // All other svm objects
            case _ => {}
        }
        ref
    }
}

object SvmRoot extends SvmComponent {
    parent = null
    name = "svm_root"
    override def registerPhases(): Unit = {}
    override def getFullName(): String = "SvmRoot"
}

object SvmComponent {
    def getTopSvc: scala.collection.mutable.LinkedHashSet[SvmComponent] = {
        val tops = SvmRoot.children
        tops.foreach(top => top.setName(top.getTypeName()))
        tops
    }
}