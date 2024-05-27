package svm.base
import svm._
import svm.base._
import spinal.core._
import scala.collection.mutable.ArrayBuffer
import svm.svmError
import svm.svmFatal
import svm.svmMedium

class SvmComponent extends SvmObject { 
    var parent: SvmComponent = null
    var children = scala.collection.mutable.LinkedHashSet.empty[SvmObjectWrapper[SvmComponent]]
    var childrenMap = scala.collection.mutable.LinkedHashMap.empty[String, SvmObjectWrapper[SvmComponent]]
    private var registered = false

    override def getFullName(): String = {
        if (parent == null) {
            getTypeName()
        } else {
            parent.getFullName() + "." + getName()
        }
    }
    
    def printTopology(): Unit = {
        svmMedium(this.getFullName())
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
                val thisWrapper = !this
                parent = SvmRoot
                SvmRoot.children += thisWrapper
                SvmRoot.childrenObj.addOne(thisWrapper)
                SvmRoot.updateChildrenWrapperName()
            }
            SvmPhaseManager.phaseBuild.addOneTask(this)(buildPhase)
            SvmPhaseManager.phaseConnect.addOneTask(this)(connectPhase)
            SvmPhaseManager.phaseRun.addOneTask(this)(runPhase)
            SvmPhaseManager.phaseCheck.addOneTask(this)(checkPhase)
            registered = true
        }
    }
    
    def getClone(): SvmComponent = this.clone().asInstanceOf[SvmComponent]
    def removeFromTree(): Unit = {
        this.parent.children = this.parent.children.filterNot(_.objHashCode == this.hashCode())
        this.parent.childrenMap = this.parent.childrenMap.filterNot({case (name, sow) => sow.objHashCode == this.hashCode()})
    }
    
    // Magic here, 
    // iterate all the sub SVC here, from the bottom to top, same order as build_phase. 
    // This method is to build the whole SVM component tree, 
    // and set the name of the SVC instance identical to the variable name.
    // This method enable user not to have to set name/parent by hand, but automatically by SVM itself.
    override def valCallback[T](ref: T, name: String): T = {
        ref match {
            case objWrapper: SvmObjectWrapper[_] =>
                objWrapper.getActualObj match {
                    case comp: SvmComponent => 
                        comp.parent = this
                        comp.parentScope = this /// Idealy it's a SvmObject concept
                        comp.setName(name)
                        objWrapper.updateName(f"${name}#${comp.hashCode()}@${this.hashCode()}")
                        svmLow(f"objWrapper.updateName(${f"${name}#${comp.hashCode()}@${this.hashCode()}"})")
                        if (this.childrenMap == null) childrenMap = scala.collection.mutable.LinkedHashMap.empty[String, SvmComponentWrapper]
                        this.childrenMap.update(name, objWrapper.asInstanceOf[SvmComponentWrapper])
                    case obj: SvmObject => 
                        obj.setName(name) // All other svm objects
                        obj.parentScope = this
                        objWrapper.updateName(f"${name}#${obj.hashCode()}@${this.hashCode()}")
                }
                childrenObj.addOne(objWrapper.asInstanceOf[SvmObjectWrapper[SvmObject]])
            case comp: SvmComponent => 
                svmWarn(f"Unmanaged SvmComonent ${comp.toString()} by factory")
                // throw new InstantiationException("SVM Component should be wrapped.")
            case obj: SvmObject => 
                obj.setName(name) // All other svm objects
                obj.parentScope = this
                svmWarn(f"Unmanaged SvmObject ${obj.toString()} by factory")
            case _ => {}
        }
        ref
    }
    
    // clean null component
    override def postInitCallback(): this.type = {
        // if (parentScope == null) updateChildrenWrapperName()
        this.childrenMap.values.foreach(cd => this.children.addOne(cd))
        this.children.foreach(_.registerPhases())
        this
    }
}

object SvmRoot extends SvmComponent {
    parent = null
    name = "svm_root"
    override def registerPhases(): Unit = {}
    override def getFullName(): String = "SvmRoot"
}

object SvmComponent {
    def getTopSvc: scala.collection.mutable.LinkedHashSet[SvmComponentWrapper] = {
        val tops = SvmRoot.children
        tops.foreach(top => top.setName(top.getTypeName()))
        tops
    }
}