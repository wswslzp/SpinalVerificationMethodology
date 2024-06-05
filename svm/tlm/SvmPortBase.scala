package svm.tlm

import svm.base.SvmObject

abstract class SvmPortBase[REQ <: SvmObject, RSP <: SvmObject] extends SvmObject {
    def connect(exp: SvmPortBase[REQ, RSP]) : this.type 
    def >>(exp: SvmPortBase[REQ, RSP]): this.type = {connect(exp)}
}

abstract class SvmTlmPortBase[REQ <: SvmObject, RSP <: SvmObject] extends SvmPortBase[REQ, RSP] {
    protected var targetExport: Option[this.type] = None
}