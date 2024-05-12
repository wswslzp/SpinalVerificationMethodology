package svm.base

import svm.ValCallback

abstract class SvmObject extends SvmVoid with ValCallback{
  var name = "null"

  // Common methods
  def setName(name: String): this.type = {
    this.name = name
    this
  }
  def getName(): String = this.name
  def getFullName(): String = this.name

  def getInstID(): Int = this.hashCode()

  def getTypeName(): String = this.getClass().getTypeName()
  
  def valCallback[T](ref: T, name: String): T = {
    ref match {
      case obj: SvmObject => obj.setName(name)
      case _ => {}
    }
    ref
  }
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