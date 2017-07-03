/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.gousiosg.javacg.stat.test.polymorphism;

/**
 *
 * @author rbruno
 */
public class Test {
    // TestInterface
    //  TestClassA (abstract), implements mA.
    //      TestClassD, implement mB.
    //  TestClassB, implements mA.
    //      TestClassE
    //  TestClassC, implements mA and mB
    
    public static void main(String[] args) throws Exception {
        // Test 1
        TestInterface ti = new ClassD();
        ti.methodA(); // called ClassA
        ti.methodB(); // called ClassD
        // Test 2
        ClassB cb = new ClassB();
        cb.methodA(); // called B
        // Test 3
        ClassB ce = new ClassE();
        ce.methodB(); // called ClassE
        // Test 4
        ti = new ClassC();
        ti.methodA(); // called ClassC
        ti.methodB(); // called ClassC
        
    }    
}
