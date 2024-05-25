package svm.base
import scala.collection.mutable.HashMap
import java.nio.file.FileSystems
import java.nio.file.Paths
import svm._

object SvmFactory {
    type SvcPathType = String

    val objectRegistry = HashMap.empty[Int, String => SvmObject]
    val componentRegistry = HashMap.empty[Int, (String, SvmComponent) => SvmComponent]
    
    // Key type name, value: instance of override type
    val objectOverridePath = HashMap.empty[SvcPathType, (String, SvmComponent)]
    
    def matchGlob(pattern: String, input: String): Boolean = {
        val matcher = FileSystems.getDefault.getPathMatcher("glob:" + pattern)
        matcher.matches(Paths.get(input))
    }
    // override method should be called before the instance declaration/creation
    // srcInstPath is the source instance path pattern. 
    // If the instance that has path matched with the pattern, 
    // then this instance will be overriden by the destined instance
    def overrideInstByType(srcInstPathPattern: String, svcTypeName: String, dstInst: SvmComponent) = {
        objectOverridePath.update(srcInstPathPattern, (svcTypeName, dstInst))
    }
    
    def overrideTypeByType(svcTypeName: String, dstInst: SvmComponent) = overrideInstByType("*", svcTypeName, dstInst)
    
    def getActualObject(srcInst: SvmComponent): SvmComponent = {
        val srcInstPath = srcInst.getFullName()
        var ret: SvmComponent = srcInst
        objectOverridePath.foreach({
            case (pattern: SvcPathType, (srcTypeName: String, svc: SvmComponent)) =>
                if (matchGlob(pattern, srcInstPath)) {
                    if (srcInst.getTypeName() == srcTypeName) {
                        ret = svc.getClone()
                        svmHigh(f"Factory type override is succeed.")
                    }
                    if (ret.equals(srcInst)) {
                        svmError(f"Factory type override is failed.")
                    }
                }
        })
        ret
    }
    
    def main(args: Array[String]) : Unit = {
        val pat = "tb.env.drv.*"
        val path = "tb.env.drv.a.b.c"
        val path1 = "tb.env.drv_a.ab.c"
        val res = matchGlob(pat, path)
        val res1 = matchGlob(pat, path1)
        println(res, res1)
    }
}