package svm
import spinal.core.sim._

abstract class SvmPhase(phaseName: String, timeConsumable: Boolean) {
    var object_counter: Int = 0
    var skipping = false
    val phaseTasks = scala.collection.mutable.LinkedHashMap[SvmComponent, SvmPhase => Unit]()
    
    def getPhaseName = phaseName
    // Run this phase with all the phase tasks
    def run(svc: SvmComponent): Unit 
    
    def addOneTask(svc: SvmComponent)(task: SvmPhase => Unit) = {
        phaseTasks.addOne((svc, task))
    }
    
    def raiseObjection(): Unit = object_counter += 1
    def dropObjection(): Unit = {
        if (object_counter > 0) {
            object_counter -= 1
        } else {
            println(s"ERROR, object counter=$object_counter less than 0")
        }
    }
    def waitPhaseEnd(): Unit = {
        waitUntil(this.object_counter <= 0)
    }
}

class SvmUpDownPhase(phaseName : String, timeConsumable: Boolean) extends SvmPhase(phaseName, timeConsumable) {
    override def run(svc: SvmComponent): Unit = {
        if (!skipping) {
            phaseTasks.getOrElse(key = svc, default = (p: SvmPhase) => {println(f"Phase ${phaseName} NOT FOUND SVC ${svc.getFullName()}")})(this)
            svc.children.foreach { c=> 
                phaseTasks.getOrElse(key = c, default = (p: SvmPhase) => {println(f"Phase ${phaseName} NOT FOUND SVC ${c.getFullName()}")})(this)
            }
            if (timeConsumable) this.waitPhaseEnd()
        }
    }
}


class SvmDownUpPhase(phaseName : String, timeConsumable: Boolean) extends SvmPhase(phaseName, timeConsumable) {
    override def run(svc: SvmComponent): Unit = {
        if (!skipping) {
            svc.children.foreach { c=> 
                phaseTasks.getOrElse(key = c, default = (p: SvmPhase) => {println(f"Phase ${phaseName} NOT FOUND SVC ${c.getFullName()}")})(this)
            }
            phaseTasks.getOrElse(key = svc, default = (p: SvmPhase) => {println(f"Phase ${phaseName} NOT FOUND SVC ${svc.getFullName()}")})(this)
            if (timeConsumable) this.waitPhaseEnd()
        }
    }
}
