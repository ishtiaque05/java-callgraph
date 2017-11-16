/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.gousiosg.javacg.stat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;

/**
 *  TODO - do some docs. here.
 * @author rbruno
 */
public class CollisionFinder {
    
    public static boolean debug = false;
    
    static class Method {
        // Eg. pck1.pk2.method
        // Note, no args are kept.
        String signature = null;
        Integer mid = null;
        boolean visited = false;
        
        public Method(String signature, Integer s) {
            this.signature = signature;
            mid = s;
        }
    }
    
    static class Allocation {
        Method method = null;
        Integer nid = null;
        
        public Allocation(Method m, Integer s) {
            method = m;
            nid = s;
        }
        
    }
    
    static class Context {
        Method method = null;
        Integer summary = 0;

        public Context(Method m, Integer s) {
            method = m;
            summary = s;
        }
    }
    
    // method signature -> method
    public static HashMap<String, Method> signature2id = new HashMap<>();
    // method id -> method
    public static HashMap<String, Method> id2signature = new HashMap<>();
    // method id -> method id
    public static HashMap<Method, List<Method>> callmap = new HashMap<>();
    // method id -> alloc id
    public static HashMap<Method, List<Allocation>> allocs = new HashMap<>();
    // <context><alloc site id> -> number of conflicts
    public static HashMap<String, Integer> conflicts = new HashMap<>();
    
    public static void loadMethods(String filename) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            // Note: expected line syntax: 
            // MID:<id>:<class>.<method>(<args>)<return type>
            while ((line = br.readLine()) != null) {
                if (debug) {
                    System.err.println("Processing line " + line + "?");
                }
                String[] splits = line.split(":");
                String mid = splits[1];
                String signature = splits[2].split("\\(")[0];
                if (!signature2id.containsKey(signature)) {
                    signature2id.put(
                            signature, 
                            new Method(signature,  Integer.valueOf(mid, 16)));
                }
                if (!id2signature.containsKey(mid)) {
                    id2signature.put(
                            mid, 
                            new Method(signature,  Integer.valueOf(mid, 16)));
                }
            }
        }
    }
    
    public static void loadCalls(String filename) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            // Note: expected line syntax: 
            // M:<caller class>:<caller method name>(<args>)#<line no> [call type] <called class>:<called method name>(<args>)
            while ((line = br.readLine()) != null) {
                if (debug) {
                    System.err.println("Processing line " + line);
                }
                String[] splits = line.split(" ");
                String caller = splits[0].split("#")[0].substring(2).replace(":", ".").split("\\(")[0];;
                String callee = splits[2].replace(":", ".").split("\\(")[0];;
                
                Method callerMethod = signature2id.get(caller);
                if (callerMethod == null) {
                    if (debug) {
                        System.err.println("ERR: unknown method signature: " + caller);
                    }
                    continue;
                }
                
                Method calleeMethod = signature2id.get(callee);
                if (calleeMethod == null) {
                    if (debug) {
                        System.err.println("ERR: unknown method signature: " + callee);
                    }
                    continue;
                }
                
                if (!callmap.containsKey(callerMethod)) {
                    callmap.put(callerMethod, new ArrayList<>());
                }
                callmap.get(callerMethod).add(calleeMethod);
            }
        }
    }
    
    public static void resetVisited() throws Exception {
        for (Method m : signature2id.values()) {
            m.visited = false;
        }
    }
    
    public static void loadAllocs(String filename) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            // Note: expected line syntax: 
            // NID:<id>:<class>.<method>(<args>)<return>:<line no>
            while ((line = br.readLine()) != null) {
                if (debug) {
                    System.err.println("Processing line " + line);
                }
                String[] splits = line.split(":");
                String nid = splits[1];
                String signature = splits[2].split("\\(")[0];
                Method m = signature2id.get(signature);
                if (m == null) {
                    if (debug) {
                        System.err.println("ERR: unknown method signature: " + signature);
                    }
                    continue;
                }
                if (!allocs.containsKey(m)) {
                    allocs.put(m, new ArrayList<>());
                }
                allocs.get(m).add(new Allocation(m, Integer.valueOf(nid, 16)));
            }
        }
    }
    
    public static void checkConflict(Short nid, Short summary) {
        String context = String.format("%04X%04X", nid, summary);
        if (!conflicts.containsKey(context)) {
            conflicts.put(context, 0);
        } else {
            conflicts.put(context,conflicts.get(context) + 1);
        }
    }
    
    public static void visitMethod(Context context) throws Exception {
        Short summary = new Integer(context.summary + context.method.mid).shortValue();
        context.method.visited = true;

        if (allocs.get(context.method) == null) {
            return;
        }
        for (Allocation alloc : allocs.get(context.method)) {
            if (debug) {
                System.err.println(String.format("Visiting (prev = %04X mid = %04X context = %04X%04X): %s",
                        context.summary, context.method.mid, alloc.nid.shortValue(), summary, context.method.signature));
            }
            checkConflict(alloc.nid.shortValue(), summary);
        }
    }
    
    public static void findCollisions() throws Exception {
        int counter = 0;
        int size = callmap.size();
        for (Entry<Method, List<Method>> entry : callmap.entrySet()) {
            counter++;
            if (debug) {
                System.err.println(String.format("Processing (%d out of %d): %s", counter, size, entry.getKey().signature));
            }
            resetVisited();
            Stack<Context> stack = new Stack<>();
            stack.push(new Context(entry.getKey(), 0));
            while(!stack.empty()) {
                Context current = stack.pop();
                if (current.method.visited) {
                    continue;
                }
                visitMethod(current);
                if (callmap.get(current.method) == null) {
                    continue;
                }
                for (Method child : callmap.get(current.method)) {
                    stack.push(new Context(child, current.summary + current.method.mid));
                }
            }
        }        
    }
    
    // Args:
    // args[0]: method ID to method signature file
    // args[1]: alloc id to method signature file
    // args[2]: caller class&method to called class&method
    public static void main(String[] args) throws Exception {
        loadMethods(args[0]);
        loadAllocs(args[1]);
        loadCalls(args[2]);
        findCollisions();
        
        System.out.println("Printing conflicts...");
        for (Entry<String, Integer> entry : conflicts.entrySet()) {
            if (entry.getValue() > 0) {
                System.out.println(String.format("%s %d", entry.getKey(), entry.getValue()));
            }
        }
        System.out.println("Printing conflicts...Done");
        
    }
}
