package svm.base

// 每个SVC有一个特定的phase task，有一个特定的phasae runner执行这个task
abstract class SvmPhaseRunner(phaseName: String, phaseFunc: () => Unit) {
}

class SvmPhaseFormerRunner