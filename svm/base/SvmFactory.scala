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
    val typeCreatorMap = scala.collection.mutable.LinkedHashMap.empty[String, () => SvmObject]
    
    def addOneTypeCreator[T<:SvmObject](name: String, creator: => T): Unit = {
        typeCreatorMap.update(name, () => creator)
    }
    def getCreatorByTypeName(name: String): Option[()=>SvmObject] = {
        typeCreatorMap.get(name).asInstanceOf[Option[()=>SvmObject]]
    }

    def matchGlob(pattern: String, input: String): Boolean = {
        val matcher = FileSystems.getDefault.getPathMatcher("glob:" + pattern)
        matcher.matches(Paths.get(input))
    }
    
    def overrideInstByName[T<:SvmObject](name: String, newObjCreator: => T)(implicit t: ClassTag[T]): Unit = {
        val targetObj = newObjCreator // To create a new object each time call this function
        val targetObjWrapper = !targetObj
        val _srcObj = SvmObjectWrapper.objNameInstMap.get(name)
        _srcObj match {
            case Some(srcObj) => 
                val srcObjType = srcObj.getTypeName()
                addOneTypeCreator(srcObjType, newObjCreator)
                if (targetObj.isInstanceOf[srcObj.type]) {
                    targetObj match {
                        case targetComp: SvmComponent => 
                            val srcComp = srcObj.asInstanceOf[SvmComponent]
                            targetComp.setName(srcComp.getName())
                            val targetCompWrapper = targetObjWrapper.asInstanceOf[SvmComponentWrapper]
                            targetComp.parent = srcComp.parent
                            targetComp.parent.children.addOne(targetCompWrapper)
                            targetComp.parent.childrenMap.addOne((name, targetCompWrapper))
                            srcComp.removeFromTree()

                            targetComp.parentScope = srcComp.parentScope
                            targetComp.parentScope.childrenObj = targetComp.parentScope.childrenObj.filterNot(_.obj.getInstID() == srcComp.getInstID()).addOne(targetCompWrapper)

                            SvmPhaseManager.removeOneComponentFromAllPhases(srcComp)
                            SvmPhaseManager.addOneComponent(targetComp)
                            targetCompWrapper.updateName(name)
                        case obj: SvmObject => 
                            targetObj.setName(obj.getName())
                            targetObj.parentScope = srcObj.parentScope
                            targetObj.parentScope.childrenObj = targetObj.parentScope.childrenObj.filterNot(_.obj.getInstID() == srcObj.getInstID()).addOne(targetObjWrapper)
                            targetObjWrapper.updateName(name)
                        case _ => 
                    }
                }
            case None => 
                logger.error(f"$name does not has corresponding object, will exit.")
                throw new NoSuchElementException
        }
        targetObjWrapper.updateChildrenWrapperName()
    }
    
    // override method should be called before the instance declaration/creation
    // srcInstPath is the source instance path pattern. 
    // If the instance that has path matched with the pattern, 
    // then this instance will be overriden by the destined instance
    // TODO: Glob pattern is not correctly, should work with type checking method
    def overrideInstByGlobPattern[T<:SvmObject](pattern: String, newObjCreator: => T)(implicit t: ClassTag[T]): Unit = {
        SvmObjectWrapper.objNameInstMap.keys.filter(matchGlob(pattern, _)).foreach({matchedName => 
            overrideInstByName(matchedName, newObjCreator)
        })
    }
    def overrideInstByRegexPattern[T<:SvmObject](pattern: String, newObjCreator: => T)(implicit t: ClassTag[T]): Unit = {
        SvmObjectWrapper.objNameInstMap.keys.filter(_.matches(pattern)).foreach({matchedName => 
            overrideInstByName(matchedName, newObjCreator)
        })
    }
    def overrideInstByTypeAndGlobPattern[T<:SvmObject](pattern: String, oldType: String, newObjCreator: => T)(implicit t: ClassTag[T]): Unit = {

    }
}