package org.dyndns.opendemogroup.todd.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;

public class TestingMethod implements IMethod {

	private String elementName;
	private boolean constructor = false;
	private List<String> parameterNames = new ArrayList<String> ( ); 
	private List<String> parameterTypes = new ArrayList<String> ( );
	private String returnType;
	private int flags;

	public String getElementName() {
		return elementName;
	}

	public void setElementName(String value) {
		elementName = value;
	}

	public String[] getExceptionTypes() throws JavaModelException {
		return null;
	}

	public String getKey() {
		return null;
	}

	public int getNumberOfParameters() {
		return parameterNames.size();
	}

	public String[] getParameterNames() throws JavaModelException {
		String[] result = new String[parameterNames.size()];
		parameterNames.toArray(result);
		return result;
	}

	public String[] getParameterTypes() {
		String[] result = new String[parameterTypes.size()];
		parameterTypes.toArray(result);
		return result;
	}

	public void addParameter ( String name, String type ) {
		parameterNames.add(name);
		parameterTypes.add(type);
	}
	
	public String[] getRawParameterNames() throws JavaModelException {
		return null;
	}

	public String getReturnType() throws JavaModelException {
		return returnType;
	}

	public void setReturnType ( String value ) {
		returnType = value;
	}
	
	public String getSignature() throws JavaModelException {
		return null;
	}

	public ITypeParameter getTypeParameter(String name) {
		return null;
	}

	public String[] getTypeParameterSignatures() throws JavaModelException {
		return null;
	}

	public ITypeParameter[] getTypeParameters() throws JavaModelException {
		return null;
	}

	public boolean isConstructor() throws JavaModelException {
		return constructor;
	}

	public void setConstructor ( boolean value ) {
		constructor = value;
	}
	
	public boolean isMainMethod() throws JavaModelException {
		return false;
	}

	public boolean isResolved() {
		return false;
	}

	public boolean isSimilar(IMethod method) {
		return false;
	}

	public String[] getCategories() throws JavaModelException {
		return null;
	}

	public IClassFile getClassFile() {
		return null;
	}

	public ICompilationUnit getCompilationUnit() {
		return null;
	}

	public IType getDeclaringType() {
		return null;
	}

	public int getFlags() throws JavaModelException {
		return flags;
	}
	
	public void setFlags ( int value ) {
		flags = value;
	}

	public ISourceRange getJavadocRange() throws JavaModelException {
		return null;
	}

	public ISourceRange getNameRange() throws JavaModelException {
		return null;
	}

	public int getOccurrenceCount() {
		return 0;
	}

	public IType getType(String name, int occurrenceCount) {
		return null;
	}

	public boolean isBinary() {
		return false;
	}

	public boolean exists() {
		return true;
	}

	public IJavaElement getAncestor(int ancestorType) {
		return null;
	}

	public String getAttachedJavadoc(IProgressMonitor monitor) throws JavaModelException {
		return null;
	}

	public IResource getCorrespondingResource() throws JavaModelException {
		return null;
	}

	public int getElementType() {
		return IJavaElement.METHOD;
	}

	public String getHandleIdentifier() {
		return null;
	}

	public IJavaModel getJavaModel() {
		return null;
	}

	public IJavaProject getJavaProject() {
		return null;
	}

	public IOpenable getOpenable() {
		return null;
	}

	public IJavaElement getParent() {
		return null;
	}

	public IPath getPath() {
		return null;
	}

	public IJavaElement getPrimaryElement() {
		return null;
	}

	public IResource getResource() {
		return null;
	}

	public ISchedulingRule getSchedulingRule() {
		return null;
	}

	public IResource getUnderlyingResource() throws JavaModelException {
		return null;
	}

	public boolean isReadOnly() {
		return false;
	}

	public boolean isStructureKnown() throws JavaModelException {
		return true;
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

	public String getSource() throws JavaModelException {
		return null;
	}

	public ISourceRange getSourceRange() throws JavaModelException {
		return null;
	}

	public void copy(IJavaElement container, IJavaElement sibling, String rename, boolean replace, IProgressMonitor monitor) throws JavaModelException {
		
	}

	public void delete(boolean force, IProgressMonitor monitor) throws JavaModelException {
		
	}

	public void move(IJavaElement container, IJavaElement sibling, String rename, boolean replace, IProgressMonitor monitor) throws JavaModelException {
	
	}

	public void rename(String name, boolean replace, IProgressMonitor monitor) throws JavaModelException {
		
	}

	public IJavaElement[] getChildren() throws JavaModelException {
		return null;
	}

	public boolean hasChildren() throws JavaModelException {
		return false;
	}
}
