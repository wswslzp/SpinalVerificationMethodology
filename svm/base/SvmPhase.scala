package svm.base
import spinal.core._
import spinal.core.sim._
import spinal.sim.SimThread
import svm._
import svm.logger
import svm.logger
import svm.logger
import svm.logger
import svm.logger

class PhaseEndWithNoObjection extends Exception
abstract class SvmPhase(phaseName: String, timeConsumable: Boolean) {
    private var object_counter: Int = 0
    var skipping = false
    val phaseTasks = scala.collection.mutable.LinkedHashMap[SvmComponent, SvmPhase => Unit]()
    val threads = scala.collection.mutable.ArrayBuffer[SimThread]()
    private var autoObjectSvc = null.asInstanceOf[SvmComponent]
    
    def getPhaseName = phaseName
    // Run this phase with all the phase tasks
    def run(svc: SvmComponent): Unit = {
        if (!skipping) {
            initiateAllTasks(svc)
            if (timeConsumable) this.waitPhaseEnd()
        }
    }
    
    def addOneTask(svc: SvmComponent)(task: SvmPhase => Unit) = {
        phaseTasks.addOne((svc, task))
    }
    def removeOneComponent(svc: SvmComponent): Unit = {
        phaseTasks.remove(svc)
    }
    
    def raiseObjection(num: Int = 1): Unit = {
        logger.debug(f"Raise objection, current obj counter = ${object_counter}")
        object_counter += num
    }
    def dropObjection(num: Int = 1): Unit = {
        if (object_counter > 0) {
            object_counter -= num
            logger.debug(f"Drop objection, current obj counter = ${object_counter}")
        } else {
            logger.error(s"ERROR, object counter=$object_counter less than 0", new RuntimeException)
        }
    }
    def setAutoObjection(svc: SvmComponent): Unit = {
        if (!timeConsumable) {
            logger.error(f"Objection should be raised/dropped in a time-comsumable phase, not in ${phaseName} phase!!!", new RuntimeException)
        } else if (autoObjectSvc == null) {
            autoObjectSvc = svc
            logger.info(f"${phaseName} phase now set auto objEction")
            val originalTask = phaseTasks.get(svc)
            originalTask match {
                case None => logger.error(f"Set objection on a empty ${phaseName} phase of ${svc.getFullName()}", new RuntimeException)
                case Some(value) => 
                    def newTask(phase: SvmPhase): Unit = {
                        phase.raiseObjection()
                        value(phase)
                        phase.dropObjection()
                    }
                    phaseTasks.update(svc, newTask)
            }
        }
    }

    def initiateOneTask(task: SvmPhase => Unit): Unit = {
        if (timeConsumable) {
            val simThread = fork {
                task(this)
            }
            threads.addOne(simThread)
        } else {
            task(this)
        }
    }
    def initiateAllTasks(svc: SvmComponent): Unit
    def waitPhaseEnd(): Unit = {
        val termination = fork {
            delayed(0 ps) {
                logger.info(f"Checking objection counter...  ${object_counter}")
                if (object_counter == 0) logger.info(f"No one raise objection, skip ${phaseName} phase.")
            }
            waitUntil(object_counter == 0)
            threads.foreach(_.terminate())
        }
        termination.join()
        logger.info(f"End ${getPhaseName} phase")
    }
}

class SvmUpDownPhase(phaseName : String, timeConsumable: Boolean) extends SvmPhase(phaseName, timeConsumable) {
    override def initiateAllTasks(svc: SvmComponent): Unit = {
        val task = phaseTasks.getOrElse(key = svc, default = (p: SvmPhase) => {
            logger.error(f"Phase ${phaseName} NOT FOUND SVC ${svc.getFullName()}", new RuntimeException)
        })
        logger.debug(f"Initiating ${svc.getFullName()} task for ${this.getPhaseName} phase.")
        initiateOneTask(task)
        svc.children.foreach(c => initiateAllTasks(c))
    }
}


class SvmDownUpPhase(phaseName : String, timeConsumable: Boolean) extends SvmPhase(phaseName, timeConsumable) {
    override def initiateAllTasks(svc: SvmComponent): Unit = {
        val task = phaseTasks.getOrElse(key = svc, default = (p: SvmPhase) => {
            logger.error(f"Phase ${phaseName} NOT FOUND SVC ${svc.getFullName()}", new RuntimeException)
        })
        logger.debug(f"Initiating ${svc.getFullName()} task for ${this.getPhaseName} phase.") 
        svc.children.foreach(c => initiateAllTasks(c))
        initiateOneTask(task)
    }
}
