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
public class ClassB implements TestInterface {
    
    int i = 10;

    @Override
    public void methodA() {
        methodB();
        System.out.println("A");
    }

    @Override
    public void methodB() {
        i--;
        if (i > 0) {
            methodA();
        }
        System.out.println("B");
    }
    
    
    
}
