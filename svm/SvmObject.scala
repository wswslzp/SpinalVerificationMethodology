package svm

abstract class SvmObject(val name: String) extends SvmVoid {
  
  // Common methods
  def setName(): Unit // name cannot be set again
  def getName(): String = this.name
  def getFullName(): String 

  def getInstID(): Int = this.hashCode()

  // 
}

object SvmObject {
  private var seed: Long = scala.util.Random.nextLong()

  def getSeeding(): Long = seed
  def setSeeding(sed: Long): Unit = {
    seed = sed
  }

  // def getType(): SvmObjectWrapper TODO: 
}