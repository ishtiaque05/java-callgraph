/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.gousiosg.javacg.stat;

import static gr.gousiosg.javacg.stat.MethodVisitor.argumentList;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ReferenceType;

/**
 *
 * @author rbruno
 */
public class MethodCall {
    // Class where the call is performed (caller)
    private JavaClass jc;
    // Reference type used to call.
    private ReferenceType refType;
    // Bytecode level instruction used to call.
    InvokeInstruction inst;
    // Constant pool of the class where the call is performed.
    ConstantPoolGen cp;
    // Method where the call is performed (caller)
    MethodGen mg;
    // Byte code index of this call.
    int bci;
    
    public MethodCall(JavaClass jc, ReferenceType refType, InvokeInstruction inst, ConstantPoolGen cp, MethodGen mg, int bci) {
        this.jc = jc;
        this.refType = refType;
        this.inst = inst;
        this.cp = cp;
        this.mg = mg;
        this.bci = bci;
    }
    
    public String getMethodName() {
        return inst.getMethodName(cp);
    }
    
    public String getCallSignature() {
        return getCallSignature(inst.getReferenceType(cp).toString());
    }
    
    public String getCallSignature(String refType) {
        return String.format("M:%s:%s(%s)#%d [X] %s:%s(%s)",
                jc.getClassName(),
                mg.getName(),
                MethodVisitor.argumentList(mg.getArgumentTypes()),
                bci,
                refType,
                inst.getMethodName(cp),
                argumentList(inst.getArgumentTypes(cp)));
    }
}
