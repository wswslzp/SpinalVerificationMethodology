package svm.base
import svm._
import svm.ValCallback
import svm.PostInitCallback
import svm.factory

abstract class SvmObject extends SvmVoid with ValCallback with PostInitCallback {
  private val createdTimeStamp = System.currentTimeMillis()
  var name = "nullObject"
  var parentScope: SvmObject = null
  var childrenObj = scala.collection.mutable.LinkedHashSet.empty[SvmObjectWrapper[SvmObject]]

  // Common methods
  def setName(name: String): this.type = {
    logger.trace("setting name")
    this.name = name
    this
  }
  def getName(): String = this.name
  def getFullName(): String = {
    if (parentScope != null) {
      parentScope match {
        case comp: SvmComponent =>
          comp.getFullName() + "." + name
        case obj: SvmObject => obj.getFullName() + "." + name
        case _              => name
      }
    } else this.name
  }

  def getInstID(): Long = this.hashCode() ^ createdTimeStamp

  def getTypeName(): String = this.getClass().getTypeName()

  def valCallback[T](ref: T, name: String): T = {
    ref match {
      case objWrapper: SvmObjectWrapper[_] =>
        objWrapper.getActualObj match {
          case obj: SvmObject =>
            obj.setName(name)
            obj.parentScope = this
            objWrapper.updateName(f"${name}#${obj.getInstID()}@${this.getInstID()}")
          case _ => {}
        }
        childrenObj.addOne(objWrapper.asInstanceOf[SvmObjectWrapper[SvmObject]])
      case obj: SvmObject =>
        obj.setName(name)
        obj.parentScope = this
        logger.warn(f"Unmanaged SvmObject ${obj.toString()} by factory")
      case _ => {}
    }
    ref
  }

  def updateChildrenWrapperName(): Unit = {
    childrenObj.foreach({ obj =>
      obj.updateName(obj.getFullName())
      obj.updateChildrenWrapperName()
    })
  }

  override def postInitCallback(): this.type = { this }

  def create = {
    logger.trace(f"Creating")
    val creator = SvmFactory.getCreatorByTypeName(this.getTypeName())
    creator match {
      case Some(trueCreator) => 
        val trueObj = trueCreator().asInstanceOf[this.type]
        SvmObjectWrapper.build(trueObj).asInstanceOf[SvmObjectWrapper[this.type]]
      case None => 
        SvmObjectWrapper.build(this).asInstanceOf[SvmObjectWrapper[this.type]]
    }
  }
  def unary_! = create
  def ! = create
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
