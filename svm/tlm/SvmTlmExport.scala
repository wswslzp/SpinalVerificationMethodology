package svm.tlm

import svm.logger
import svm.base.SvmObject

// Svm TLM1.0 one-to-one transport SvmTlmPortBase
class SvmTlmExport[REQ <: SvmObject, RSP <: SvmObject] extends SvmTlmPortBase[REQ, RSP] {
    override def connect(exp: SvmPostBase[REQ, RSP]) : this.type = {
        logger.trace(f"${this.getFullName()} connects ${exp.getFullName()}")
        targetExport = Some(exp.asInstanceOf[this.type]) // TODO: considering different type connections
        this
    }
}

/**
  * Conceptually, it's the initiator port
  */
class SvmTlmPort[REQ<:SvmObject, RSP<:SvmObject] extends SvmTlmExport[REQ, RSP]

/**
  * Conceptually, it's the terminator port
  */
class SvmTlmImp[REQ<:SvmObject, RSP<:SvmObject] extends SvmTlmExport[REQ, RSP]
