package svm
import spinal.core.sim._

class SvmPhase {
    var object_counter: Int = 0
    
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
