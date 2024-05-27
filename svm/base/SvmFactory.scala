package svm.base
import scala.collection.mutable.HashMap
import java.nio.file.FileSystems
import java.nio.file.Paths
import svm._

object SvmFactory {
    def matchGlob(pattern: String, input: String): Boolean = {
        val matcher = FileSystems.getDefault.getPathMatcher("glob:" + pattern)
        matcher.matches(Paths.get(input))
    }
    
    def overrideInstByName[T<:SvmObject](name: String, newObjCreator: => T): Unit = {
        val targetObj = newObjCreator // To create a new object each time call this function
        targetObj match {
            case targetComp: SvmComponent => 
                val srcComp = SvmObjectWrapper.objNameInstMap.get(name).get.asInstanceOf[SvmComponent]
                targetComp.parent = srcComp.parent
                targetComp.parent.children.addOne(targetComp.unary_!.asInstanceOf[SvmComponentWrapper])
                targetComp.children = srcComp.children
                srcComp.removeFromTree()
                SvmPhaseManager.removeOneComponentFromAllPhases(srcComp)
                SvmPhaseManager.addOneComponent(targetComp)
                SvmObjectWrapper.objNameInstMap.update(name, targetObj)
            case obj: SvmObject => 
                SvmObjectWrapper.objNameInstMap.update(name, targetObj)
            // val srcObj = SvmObjectWrapper.objNameInstMap.get(matchedName).get
            // if (srcObj.isInstanceOf[T]) { // TODO: Fix type check. comment due to type erasure
                
            // }
        }
    }
    
    // override method should be called before the instance declaration/creation
    // srcInstPath is the source instance path pattern. 
    // If the instance that has path matched with the pattern, 
    // then this instance will be overriden by the destined instance
    def overrideInstByGlobPattern[T<:SvmObject](pattern: String, newObjCreator: => T): Unit = {
        SvmObjectWrapper.objNameInstMap.keys.filter(matchGlob(pattern, _)).foreach({matchedName => 
            overrideInstByName(matchedName, newObjCreator)
        })
    }
    def overrideInstByRegexPattern[T<:SvmObject](pattern: String, newObjCreator: => T): Unit = {
        SvmObjectWrapper.objNameInstMap.keys.filter(_.matches(pattern)).foreach({matchedName => 
            overrideInstByName(matchedName, newObjCreator)
        })
    }
}