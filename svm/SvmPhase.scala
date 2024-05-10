package svm
import spinal.core.sim._
import spinal.sim.SimThread
import org.log4s._

abstract class SvmPhase(phaseName: String, timeConsumable: Boolean) {
    var object_counter: Int = 0
    var skipping = false
    val phaseTasks = scala.collection.mutable.LinkedHashMap[SvmComponent, SvmPhase => Unit]()
    val threads = scala.collection.mutable.ArrayBuffer[SimThread]()
    
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
            svmLogger.error(s"ERROR, object counter=$object_counter less than 0")
        }
    }
    def initiateTask(task: SvmPhase => Unit): Unit = {
        if (timeConsumable) {
            val simThread = fork {
                task(this)
            }
            threads.addOne(simThread)
        } else {
            task(this)
        }
    }
    def waitPhaseEnd(): Unit = {
        forkJoin(
            () => {
                waitUntil(this.object_counter <= 0)
                threads.foreach(_.terminate())
            },
            () => {threads.foreach(_.join())}
        )
        svmLogger.info(f"End ${getPhaseName} phase")
    }
}

class SvmUpDownPhase(phaseName : String, timeConsumable: Boolean) extends SvmPhase(phaseName, timeConsumable) {
    override def run(svc: SvmComponent): Unit = {
        if (!skipping) {
            val task = phaseTasks.getOrElse(key = svc, default = (p: SvmPhase) => {
                svmLogger.error(f"Phase ${phaseName} NOT FOUND SVC ${svc.getFullName()}")
            })
            initiateTask(task)
            svc.children.foreach { c=> 
                val task = phaseTasks.getOrElse(key = c, default = (p: SvmPhase) => {
                    svmLogger.error(f"Phase ${phaseName} NOT FOUND SVC ${c.getFullName()}")
                })
                initiateTask(task)
            }
            if (timeConsumable) this.waitPhaseEnd()
        }
    }
}


class SvmDownUpPhase(phaseName : String, timeConsumable: Boolean) extends SvmPhase(phaseName, timeConsumable) {
    override def run(svc: SvmComponent): Unit = {
        if (!skipping) {
            svc.children.foreach { c=> 
                val task = phaseTasks.getOrElse(key = c, default = (p: SvmPhase) => {println(f"Phase ${phaseName} NOT FOUND SVC ${c.getFullName()}")})
                initiateTask(task)
            }
            val task = phaseTasks.getOrElse(key = svc, default = (p: SvmPhase) => {println(f"Phase ${phaseName} NOT FOUND SVC ${svc.getFullName()}")})
            initiateTask(task)
            if (timeConsumable) this.waitPhaseEnd()
        }
    }
}
