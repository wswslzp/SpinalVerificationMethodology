package svm.base

class SvmObjectWrapper[+T <: SvmObject](val obj: T, val objHashCode: Int) {
    def getActualObj = {
        val objInstName = SvmObjectWrapper.objHashNameMap.getOrElse(this.objHashCode, SvmObjectWrapper.objTempName)
        val tryGetSvmObj = SvmObjectWrapper.objNameInstMap.get(objInstName)
        if (tryGetSvmObj == None) throw new NoSuchElementException
        else tryGetSvmObj.get.asInstanceOf[T]
    }
    def updateName(newName: String): Unit = {
        SvmObjectWrapper.objHashNameMap.update(this.objHashCode, newName)
        SvmObjectWrapper.objNameInstMap.update(newName, this.obj)
    }
}

object SvmObjectWrapper {
    val objHashNameMap = scala.collection.mutable.LinkedHashMap.empty[Int, String]
    val objNameInstMap = scala.collection.mutable.LinkedHashMap.empty[String, SvmObject]
    val objTempName = "tempObj"
    def build[T <: SvmObject](obj: T): SvmObjectWrapper[T] = {
        val objHashCode = obj.hashCode()
        objHashNameMap.update(objHashCode, objTempName)
        objNameInstMap.update(objTempName, obj)
        new SvmObjectWrapper(obj, objHashCode)
    }
}