/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.tools.internal;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

public class ReflectClass extends ReflectItem implements JNIClass {
	Class clazz;
	ReflectField[] fields;
	ReflectMethod[] methods;
	MetaData metaData;
	String sourcePath, source;
	ASTNode ast;

public ReflectClass(Class clazz) {
	this(clazz, null, null);
}

public ReflectClass(Class clazz, MetaData data, String sourcePath) {
	this.clazz = clazz;
	this.metaData = data;
	this.sourcePath = sourcePath;
}

void checkMembers() {
	if (fields != null) return;
	long time = System.currentTimeMillis();
	Field[] fields = clazz.getDeclaredFields();
	this.fields = new ReflectField[fields.length];
	for (int i = 0; i < fields.length; i++) {
		this.fields[i] = new ReflectField(this, fields[i]);
	}
	Method[] methods = clazz.getDeclaredMethods();
	this.methods = new ReflectMethod[methods.length];
	for (int i = 0; i < methods.length; i++) {
		this.methods[i] = new ReflectMethod(this, methods[i]);
	}
	if (System.currentTimeMillis() - time > 100) {
	//	Thread.dumpStack();
	//	System.err.println("time=" + (System.currentTimeMillis() - time) + " " +clazz.getName());
	}
}

ASTNode getDOM() {
	if (ast != null) return ast;
	source = JNIGenerator.loadFile(sourcePath);
	ASTParser parser = ASTParser.newParser(AST.JLS3);
	parser.setSource(source.toCharArray());
	return ast = parser.createAST(null);
}

public int hashCode() {
	return clazz.hashCode();
}

public boolean equals(Object obj) {
	if (!(obj instanceof ReflectClass)) return false;
	return ((ReflectClass)obj).clazz.equals(clazz);
}

public JNIField[] getDeclaredFields() {
	checkMembers();
	return fields;
}

public JNIMethod[] getDeclaredMethods() {
	checkMembers();
	return methods;
}

public String getName() {
	return clazz.getName();
}

public JNIClass getSuperclass() {
	Class superclazz = clazz.getSuperclass();
	String path = new File(sourcePath).getParent() + "/" + superclazz.getSimpleName() + ".java";
	return new ReflectClass(superclazz, metaData, path);
}

public String getSimpleName() {
	String name = clazz.getName();
	int index = name.lastIndexOf('.') + 1;
	return name.substring(index, name.length());
}

public String getExclude() {
	return (String)getParam("exclude");
}

public String getMetaData() {
	String key = JNIGenerator.toC(clazz.getName());
	return metaData.getMetaData(key, "");
}

public void setExclude(String str) { 
	setParam("exclude", str);
}

public void setMetaData(String value) {
	String key = JNIGenerator.toC(clazz.getName());
	metaData.setMetaData(key, value);
}

public String toString() {
	return clazz.toString();
}

}
