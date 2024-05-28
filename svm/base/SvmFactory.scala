package svm.base
import scala.collection.mutable.HashMap
import java.nio.file.FileSystems
import java.nio.file.Paths
import svm._
import svm.logger
import svm.SvmComponentWrapper
import svm.SvmComponentWrapper
import scala.reflect.ClassTag

object SvmFactory {
    def matchGlob(pattern: String, input: String): Boolean = {
        val matcher = FileSystems.getDefault.getPathMatcher("glob:" + pattern)
        matcher.matches(Paths.get(input))
    }
    
    def overrideInstByName[T<:SvmObject](name: String, newObjCreator: => T)(implicit t: ClassTag[T]): Unit = {
        val targetObj = newObjCreator // To create a new object each time call this function
        val targetObjWrapper = !targetObj
        val srcObj = SvmObjectWrapper.objNameInstMap.get(name).get
        if (targetObj.isInstanceOf[T]) {
            targetObj match {
                case targetComp: SvmComponent => 
                    // FIXME: children being itself
                    val srcComp = srcObj.asInstanceOf[SvmComponent]
                    targetComp.setName(srcComp.getName())
                    val targetCompWrapper = targetObjWrapper.asInstanceOf[SvmComponentWrapper]
                    targetComp.parent = srcComp.parent
                    targetComp.parent.children.addOne(targetCompWrapper)
                    targetComp.parent.childrenMap.addOne((name, targetCompWrapper))
                    // targetComp.children = srcComp.children
                    srcComp.removeFromTree()

                    targetComp.parentScope = srcComp.parentScope
                    targetComp.parentScope.childrenObj = targetComp.parentScope.childrenObj.filterNot(_.obj.hashCode == srcComp.hashCode()).addOne(targetCompWrapper)

                    SvmPhaseManager.removeOneComponentFromAllPhases(srcComp)
                    SvmPhaseManager.addOneComponent(targetComp)
                    SvmObjectWrapper.objNameInstMap.update(name, targetComp)
                    val comp1 = SvmObjectWrapper.objNameInstMap.get(name).get.asInstanceOf[SvmComponent]
                case obj: SvmObject => 
                    targetObj.setName(obj.getName())
                    targetObj.parentScope = srcObj.parentScope
                    targetObj.parentScope.childrenObj = targetObj.parentScope.childrenObj.filterNot(_.obj.hashCode == srcObj.hashCode()).addOne(targetObjWrapper)
                    SvmObjectWrapper.objNameInstMap.update(name, targetObj)
                case _ => 
            }
        }
    }
    
    // override method should be called before the instance declaration/creation
    // srcInstPath is the source instance path pattern. 
    // If the instance that has path matched with the pattern, 
    // then this instance will be overriden by the destined instance
    // TODO: Glob pattern is not correctly, should work with type checking method
    def overrideInstByGlobPattern[T<:SvmObject](pattern: String, newObjCreator: => T)(implicit t: ClassTag[T]): Unit = {
        logger.trace(f"overrideInstByGlobPattern")
        SvmObjectWrapper.objNameInstMap.keys.filter(matchGlob(pattern, _)).foreach({matchedName => 
            overrideInstByName(matchedName, newObjCreator)
        })
    }
    def overrideInstByRegexPattern[T<:SvmObject](pattern: String, newObjCreator: => T)(implicit t: ClassTag[T]): Unit = {
        SvmObjectWrapper.objNameInstMap.keys.filter(_.matches(pattern)).foreach({matchedName => 
            overrideInstByName(matchedName, newObjCreator)
        })
    }
}