package svm.base

abstract class SvmObject(name: String) extends SvmVoid {
  
  // Common methods
  // def setName(): Unit // name cannot be set again
  def getName(): String = this.name
  def getFullName(): String = this.name

  def getInstID(): Int = this.hashCode()

  def getTypeName(): String = getClass().getTypeName()
}

object SvmObject {
  private var seed: Long = scala.util.Random.nextLong()

  def getSeeding(): Long = seed
  def setSeeding(sed: Long): Unit = {
    seed = sed
  }

  // def getTypeId[T <: ]
  // def getType(): SvmObjectWrapper TODO: 
}