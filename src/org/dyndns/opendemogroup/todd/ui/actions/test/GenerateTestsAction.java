package org.dyndns.opendemogroup.todd.ui.actions.test;

import static org.junit.Assert.*;

import java.text.MessageFormat;

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
import org.junit.Test;

/**
 * A class to test the class
 * {@link org.dyndns.opendemogroup.todd.ui.actions.GenerateTestsAction}.
 */
public class GenerateTestsAction extends org.dyndns.opendemogroup.todd.ui.actions.GenerateTestsAction {

	private class TestingMethod implements IMethod {

		private String _ElementName;

		public String getElementName() {
			return _ElementName;
		}

		public void setElementName(String elementName) {
			_ElementName = elementName;
		}

		public String[] getExceptionTypes() throws JavaModelException {
			return null;
		}

		public String getKey() {
			return null;
		}

		public int getNumberOfParameters() {
			// TODO: Implement this later
			return 0;
		}

		public String[] getParameterNames() throws JavaModelException {
			// TODO: Implement this later
			return null;
		}

		public String[] getParameterTypes() {
			// TODO: Implement this later
			return null;
		}

		public String[] getRawParameterNames() throws JavaModelException {
			return null;
		}

		public String getReturnType() throws JavaModelException {
			// TODO: Implement this later
			return null;
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
			return false;
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
			// TODO: Implement this later
			return 0;
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
		
	/**
	 * A rather lame test but Java's lack of multi-line strings makes testing
	 * this rather difficult and so most of the code being tested is also in 
	 * here.
	 */
	@Test
	public void generateTestMethodContents_Typical ( ) {
		TestingMethod t = new TestingMethod ();
		String methodName = "Unformat";
		t.setElementName(methodName);
		String actual = generateTestMethodContents(t);
		String testMethodTemplate =
			"{1}/**{1} * Tests the <i>{0}</i> method with {1} * TODO: write about scenario{1} */{1}@Test public void {0}_TODO ( ) '{' {1}\t// TODO: invoke {0} and assert properties of its effects/output{1}\tfail ( \"Test not yet written\" ); {1}}{1}";
		String newLine = System.getProperty("line.separator");
		String expected = 
			MessageFormat.format( testMethodTemplate, methodName, newLine );
		assertEquals(expected, actual);
	}

}
