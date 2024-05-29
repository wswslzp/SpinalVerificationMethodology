package svm.base
import svm.logger
import scala.reflect.ClassTag

class SvmObjectWrapper[+T <: SvmObject](val obj: T, val objHashCode: Int) {
    def getActualObj = {
        val tryGetObjInstName = SvmObjectWrapper.objHashNameMap.get(objHashCode)
        tryGetObjInstName match {
            case Some(objInstName) => 
                val tryGetSvmObj = SvmObjectWrapper.objNameInstMap.get(objInstName)
                logger.trace(f"Getting Inst ${objInstName}, hash code=${this.objHashCode}")
                if (tryGetSvmObj == None) {
                    logger.trace(f"Getting Inst ${objInstName}, hash code=${this.objHashCode}")
                    throw new NoSuchElementException
                }
                else {
                    tryGetSvmObj.get.asInstanceOf[T]
                }
            case None => 
                logger.error(f"Found no SvmObject with hash code ${objHashCode}")
                throw new NoSuchElementException
        }
    }
    def updateName(newName: String): Unit = {
        val tryGetOldName = SvmObjectWrapper.objHashNameMap.get(objHashCode)
        tryGetOldName match {
            case Some(oldName) => 
                SvmObjectWrapper.objHashNameMap.update(this.objHashCode, newName)
                SvmObjectWrapper.objNameInstMap.update(newName, this.obj)
                SvmObjectWrapper.objNameInstMap.remove(oldName)
            case None => logger.error(f"Found no SvmObject with hash code ${objHashCode}", new NoSuchElementException)
        }
    }
    def setName(newName: String): Unit = {
        obj.setName(newName)
        updateName(newName)
    }
}

object SvmObjectWrapper {
    val objHashNameMap = scala.collection.mutable.LinkedHashMap.empty[Int, String]
    val objNameInstMap = scala.collection.mutable.LinkedHashMap.empty[String, SvmObject]
    val objTempName = "tempObj" // at the time build the svm object's wrapper, we don't know its variable name
    def build[T <: SvmObject](obj: T): SvmObjectWrapper[T] = {
        val objHashCode = obj.hashCode()
        objHashNameMap.update(objHashCode, objTempName)
        objNameInstMap.update(objTempName, obj)
        new SvmObjectWrapper(obj, objHashCode)
    }
}