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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;

/**
 * Constructs a callgraph out of a set of directories, classes and JAR archives.
 * Can combine multiple archives into a single call graph.
 *
 * @author Georgios Gousios <gousiosg@gmail.com>
 *
 */
public class JCallGraph {

    public static Set<String> visitedClasses = new HashSet<>();

    // TODO - could this go away? We have calls, it should be enough.
    public static Set<String> referencedClasses  = new HashSet<>();

    // <caller, <callee, <method call>>>
    public static Map<String, Map<String, List<MethodCall>>> calls = new HashMap<>();

    public static void addVisitedClass(JavaClass jc) {
        if (!visitedClasses.contains(jc.getClassName())) {
            visitedClasses.add(jc.getClassName());
        }
    }

    public static void addReferencedClass(String classname) {
        if (!JCallGraph.referencedClasses.contains(classname)) {
            JCallGraph.referencedClasses.add(classname);
        }
    }

    public static void processClass(String class_name) throws IOException {
        ClassParser cp = new ClassParser(class_name);
        ClassVisitor visitor = new ClassVisitor(cp.parse());
        visitor.start();
    }

    public static void processClass(String jar_name, String class_name) throws IOException {
        ClassParser cp = new ClassParser(jar_name,class_name);
        ClassVisitor visitor = new ClassVisitor(cp.parse());
        visitor.start();
    }

    public static void processJar(JarFile jar) throws IOException {
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.isDirectory())
                continue;

            if (!entry.getName().endsWith(".class"))
                continue;

            processClass(jar.getName(),entry.getName());
        }
    }

    public static void processFile(File file) {
        try {
            if (!file.exists())
                System.err.println("File " + file.getName() + " does not exist");

            else if (file.isDirectory()) {
                for (File dfile : file.listFiles())
                    processFile(dfile);
            }

            else if (file.getName().endsWith(".jar"))
                processJar(new JarFile(file));

            else if (file.getName().endsWith(".class"))
                processClass(file.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("Error while processing file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Returns true if 'clazz' contains a method declaration that matches the
     * provided 'signature'. Signature is method(args).
     * @param clazz
     * @param signature
     * @return 
     */
    public static boolean containsMethod(Class clazz, String signature) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.toString().contains(signature)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if 'type' is primitive.
     * @param type
     * @return 
     */
    public static boolean isPrimitive(String type) {
        switch(type) {
            case "byte":
            case "short":
            case "int":
            case "long":
            case "float":
            case "double":
            case "boolean":
            case "char":
                return true;
            default:
                return false;
        }
    }

    public static void main(String[] args) {
        for (String arg : args) {
            processFile(new File(arg));
        }

        // Prints warning for all referenced but not visited classes.
        for (String calledClass : referencedClasses) {
            if (!visitedClasses.contains(calledClass)) {
                System.out.println("Warning: called class not found: " + calledClass);
            }
        }

        // Deals with polymorphism.
        // For each caller
        for (Entry<String, Map<String, List<MethodCall>>> call : calls.entrySet()) {
            // For each callee
            for (Entry<String, List<MethodCall>> reference : call.getValue().entrySet()) {
                for (String visited : visitedClasses) {
                    try {
                        String refType = reference.getKey().replace("[]", "");

                        if (isPrimitive(refType)) {
                            continue;
                        }

                        if (refType.equals(visited)) {
                            continue;
                        }

                        Class visitedClass = Class.forName(visited);
                        Class calledClass = Class.forName(refType);
                        if (calledClass.isAssignableFrom(visitedClass)) {
                            // For each called method
                            for (MethodCall methodCall : reference.getValue()) {
                                if (containsMethod(visitedClass, methodCall.getJavaCallSignature())) {
                                    System.out.println(methodCall.getCallSignature(visited));
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
