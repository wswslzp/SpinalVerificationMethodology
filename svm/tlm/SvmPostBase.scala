package svm.tlm

import svm.base.SvmObject

abstract class SvmPostBase[REQ <: SvmObject, RSP <: SvmObject] extends SvmObject {
    def connect(exp: SvmPostBase[REQ, RSP]) : this.type 
    def >>(exp: SvmPostBase[REQ, RSP]): this.type = {connect(exp)}
}