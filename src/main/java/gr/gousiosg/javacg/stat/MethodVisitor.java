/*
 * Copyright (c) 2011 - Georgios Gousios <gousiosg@gmail.com>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package gr.gousiosg.javacg.stat;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.*;

/**
 * The simplest of method visitors, prints any invoked method
 * signature for all method invocations.
 *
 * Class copied with modifications from CJKM: http://www.spinellis.gr/sw/ckjm/
 */
public class MethodVisitor extends EmptyVisitor {

    JavaClass visitedClass;
    private MethodGen mg;
    private ConstantPoolGen cp;
    private String format;
    // BCI for current instruction.
    private int bci;
    // BCI for the next instruction.
    private int nbci;

    // TODO - I can visit news
    // TODO - could I see if these news escape the method?

    public MethodVisitor(MethodGen m, JavaClass jc) {
        visitedClass = jc;
        mg = m;
        cp = mg.getConstantPool();
        format = "M:" + visitedClass.getClassName() + ":" + mg.getName() + "(" + argumentList(mg.getArgumentTypes()) + "):%d"
            + " " + "(%s)%s:%s(%s)";
        bci = 0;
        nbci = 0;
    }

    public static String argumentList(Type[] arguments) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arguments.length; i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(arguments[i].toString());
        }
        return sb.toString();
    }

    public void start() {
        if (mg.isAbstract() || mg.isNative())
            return;
        for (InstructionHandle ih = mg.getInstructionList().getStart();
                ih != null; ih = ih.getNext()) {
            Instruction i = ih.getInstruction();

            if (!visitInstruction(i))
                i.accept(this);
        }
    }

    private boolean visitInstruction(Instruction i) {
        short opcode = i.getOpcode();
        bci = nbci;
        nbci += i.getLength();
        return ((InstructionConst.getInstruction(opcode) != null)
                && !(i instanceof ConstantPushInstruction)
                && !(i instanceof ReturnInstruction));
    }

    private void prepareCall(JavaClass caller, ReferenceType callee, InvokeInstruction i) {
        String callerClassName = caller.getClassName();
        String calleeClassName = callee.toString();
        MethodCall call = new MethodCall(caller, callee, i, cp, mg, bci);
        if (!JCallGraph.calls.containsKey(callerClassName)) {
            JCallGraph.calls.put(callerClassName, new HashMap<>());
        }

        if (!JCallGraph.calls.get(callerClassName).containsKey(calleeClassName)) {
            JCallGraph.calls.get(callerClassName).put(calleeClassName, new ArrayList<>());
        }

        if (!JCallGraph.calls.get(callerClassName).get(calleeClassName).contains(call)) {
            JCallGraph.calls.get(callerClassName).get(calleeClassName).add(call);
        }
    }

    @Override
    public void visitINVOKEVIRTUAL(INVOKEVIRTUAL i) {
        prepareCall(visitedClass, i.getReferenceType(cp), i);
        System.out.println(String.format(format,bci,"M",i.getReferenceType(cp),i.getMethodName(cp),argumentList(i.getArgumentTypes(cp))));
    }

    @Override
    public void visitINVOKEINTERFACE(INVOKEINTERFACE i) {
        prepareCall(visitedClass, i.getReferenceType(cp), i);
        System.out.println(String.format(format,bci,"I",i.getReferenceType(cp),i.getMethodName(cp),argumentList(i.getArgumentTypes(cp))));
    }

    @Override
    public void visitINVOKESPECIAL(INVOKESPECIAL i) {
        // Note: I don't need to handle these calls. They are always resolved at compile time.
        System.out.println(String.format(format,bci,"O",i.getReferenceType(cp),i.getMethodName(cp),argumentList(i.getArgumentTypes(cp))));
    }

    @Override
    public void visitINVOKESTATIC(INVOKESTATIC i) {
        prepareCall(visitedClass, i.getReferenceType(cp), i);
        System.out.println(String.format(format,bci,"S",i.getReferenceType(cp),i.getMethodName(cp),argumentList(i.getArgumentTypes(cp))));
    }

    @Override
    public void visitINVOKEDYNAMIC(INVOKEDYNAMIC i) {
        prepareCall(visitedClass, i.getReferenceType(cp), i);
        System.out.println(String.format(format,bci,"D",i.getType(cp),i.getMethodName(cp),
                argumentList(i.getArgumentTypes(cp))));
    }
}
