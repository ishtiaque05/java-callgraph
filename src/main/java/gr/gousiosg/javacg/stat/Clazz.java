/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.gousiosg.javacg.stat;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author rbruno
 */
public class Clazz {
    
    private String name;
    private Set<Clazz> subtypes = new HashSet<>();
    private Set<String> methods = new HashSet<>();
    
    public Clazz(String name) {
        this.name = name;
    }
    
    public void addSubtype(Clazz clazz) {
        if (!subtypes.contains(clazz)) {
            subtypes.add(clazz);
        }
    }
    public void addMethod(String method) {
        if (!methods.contains(method)) {
            methods.add(method);
        }
    }
    
    public Set<Clazz> getSubtypes() { return subtypes; }
    public Set<String> getMethods() { return methods; }
    
}
