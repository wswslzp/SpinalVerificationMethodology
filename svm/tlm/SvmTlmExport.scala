package svm.tlm

import svm.logger
import svm.base.SvmObject

// Svm TLM1.0 one-to-one transport SvmTlmPortBase
class SvmTlmExport[REQ <: SvmObject, RSP <: SvmObject] extends SvmTlmPortBase[REQ, RSP] {
    override def connect(exp: SvmPortBase[REQ, RSP]) : this.type = {
        logger.trace(f"${this.getFullName()} connects ${exp.getFullName()}")
        targetExport = Some(exp.asInstanceOf[this.type]) // TODO: considering different type connections
        this
    }
}
class SvmTlmPeekExport[REQ <: SvmObject, RSP <: SvmObject] extends SvmTlmExport[REQ,RSP] with SvmTlmPeekable[REQ,RSP]
class SvmTlmPutExport[REQ <: SvmObject, RSP <: SvmObject] extends SvmTlmExport[REQ,RSP] with SvmTlmPuttable[REQ,RSP]
class SvmTlmGetExport[REQ <: SvmObject, RSP <: SvmObject] extends SvmTlmExport[REQ,RSP] with SvmTlmGettable[REQ,RSP]

/**
  * Conceptually, it's the initiator port
  */
class SvmTlmPort[REQ<:SvmObject, RSP<:SvmObject] extends SvmTlmExport[REQ, RSP]
class SvmTlmPeekPort[REQ <: SvmObject, RSP <: SvmObject] extends SvmTlmPort[REQ,RSP] with SvmTlmPeekable[REQ,RSP]
class SvmTlmPutPort[REQ <: SvmObject, RSP <: SvmObject] extends SvmTlmPort[REQ,RSP] with SvmTlmPuttable[REQ,RSP]
class SvmTlmGetPort[REQ <: SvmObject, RSP <: SvmObject] extends SvmTlmPort[REQ,RSP] with SvmTlmGettable[REQ,RSP]

/**
  * Conceptually, it's the terminator port
  */
class SvmTlmImp[REQ<:SvmObject, RSP<:SvmObject] extends SvmTlmExport[REQ, RSP]
class SvmTlmPeekImp[REQ <: SvmObject, RSP <: SvmObject] extends SvmTlmImp[REQ,RSP] with SvmTlmPeekable[REQ,RSP]
class SvmTlmPutImp[REQ <: SvmObject, RSP <: SvmObject] extends SvmTlmImp[REQ,RSP] with SvmTlmPuttable[REQ,RSP]
class SvmTlmGetImp[REQ <: SvmObject, RSP <: SvmObject] extends SvmTlmImp[REQ,RSP] with SvmTlmGettable[REQ,RSP]
