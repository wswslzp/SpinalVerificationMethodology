package svm.base

//PhaseRunnerContainer包含所有的PhaseRunner，按照次序依次执行所有的PhaseRunner
//
object SvmPhaseManager {
    val phaseBuild = new SvmDownUpPhase("build", false)
    val phaseConnect = new SvmUpDownPhase("connect", false)
    val phaseRun = new SvmUpDownPhase("run", true)
    val phaseCheck = new SvmUpDownPhase("check", false)
    
    val phases = scala.collection.mutable.LinkedHashSet[SvmPhase](
        phaseBuild, phaseConnect, phaseRun, phaseCheck
    )
    
    def removeOneComponentFromAllPhases(svc: SvmComponent): Unit = {
        phases.foreach(_.removeOneComponent(svc))
    }
    def addOneComponent(svc: SvmComponent): Unit = {
        phaseBuild.addOneTask(svc)(svc.buildPhase)
        phaseConnect.addOneTask(svc)(svc.connectPhase)
        phaseRun.addOneTask(svc)(svc.runPhase)
        phaseCheck.addOneTask(svc)(svc.checkPhase)
    }

    def addPhase(phase: SvmPhase) : Unit = phases.addOne(phase)
    def runAllPhase(svc: SvmComponent): Unit = {
        phases.foreach(_.run(svc))
    }
}
