package org.dyndns.opendemogroup.todd.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.dyndns.opendemogroup.todd.SimpleSearchRequestor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class GenerateTestsAction implements IObjectActionDelegate {

	/**
	 * Target instances to operate on.  In our case, it will most likely be
	 * methods ({@link IMethod}) and classes ({@link IFile} and {@link IType}). 
	 */
	List _Members = null;
	
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if ( _Members != null ) {
			for ( Object each : _Members ) {
				if (each instanceof IMethod) {
					IMethod eachMethod = (IMethod) each;
					int flags = 0;
					try {
						flags = eachMethod.getFlags ( );
					}
					catch ( JavaModelException jme ) {
						// TODO: Find out when this is thrown and see if there's
						// a better reaction than simply stopping everything.
						System.out.print ( "    -> JME thrown: " );
						System.out.println ( jme );
						return;
					}
					// TODO: move this check to selectionChanged if possible
					if ( (flags & Flags.AccPrivate) != 0 ) {
						// can't call private member!
					}
					else if ( (flags & Flags.AccProtected) != 0) {
						// TODO: Also consider an abstract method
						// TODO: write a test for method only if test class subclasses us
					}
					else {
						generateTest ( eachMethod );
					}
				}
				else if (each instanceof IFile) {
					IFile eachFile = (IFile) each;
					// TODO: determine the appropriate IType and simply forward
					// to whatever the following code block will do
					// TODO: to support the previous statement, some sort of
					// adapter/factory type of abstraction could be used for
					// added sexiness.
				}
				else if (each instanceof IType) {
					// TODO: I can't seem to trigger on IType and instead I get
					// an instance of File when I think I'm on a Class...
					// This may be because I defined an extension on IFile?!?!?
					IType eachClass = (IType) each;
					// TODO: determine if eachClass is abstract and react accordingly
					// TODO: write a testcase class
					// TODO: Consider invoking the current "New JUnit Test Case" wizard
				}
			}
		}
	}

	/**
	 * Given an {@link IMethod} instance, will attempt to generate a JUnit test
	 * method for it in an appropriate associated test class.
	 * @param method The method for which a test is to be generated.
	 */
	void generateTest(IMethod method) {
		ICompilationUnit testClass = fetchAssociatedTestClass ( method );
		if (null == testClass) {
			// fetchTestClass (currently) may return nothing, thus we do nothing  
			return;
		}
		// TODO: Open an editor for testClass
		// TODO: Search for a spot to insert the new test method:
		// After last occurence of eachMethod.getName, or as the last method.
		// Use IType.createMethod
		// TODO: Consider scanning for special comments delineating test regions
	}
	

	/**
	 * Attempts to find or create an associated test class for the class in
	 * which the specified <i>testedMethod</i> is found.
	 * @param testedMethod The {@link IMethod} instance for which to obtain an
	 * associated test class.
	 * @return An {@link ICompilationUnit} which represents the associated
	 * test class.
	 */
	ICompilationUnit fetchAssociatedTestClass(IMethod testedMethod) {
		// determine method's class and package names
		IJavaElement potentialClass = testedMethod.getParent();
		IType parentClass = null;
		if (potentialClass instanceof IType) {
			parentClass = (IType) potentialClass;
		}
		if (null == parentClass) {
			// TODO: determine under which circumstances this would happen
			return null;
		}
		return fetchAssociatedTestClass(parentClass);
	}

	/**
	 * Attempts to find or create an associated test class for the class in
	 * which the specified <i>testedType</i> is found.
	 * @param testedType The {@link IType} instance for which to obtain an
	 * associated test class.
	 * @return An {@link ICompilationUnit} which represents the associated
	 * test class.
	 */
	ICompilationUnit fetchAssociatedTestClass(IType testedType) {
		String className = testedType.getElementName();
		IPackageFragment parentPackage = testedType.getPackageFragment();
		String packageName = parentPackage.getElementName();
		
		// Search for class with same name in package (package + ".test")
		String associatedTestClassName = packageName + ".test." + className;
		List<SearchMatch> results = findClass(associatedTestClassName);
		// If none are found...
		if ( null == results || 0 == results.size() ) {
			// TODO: Create an associated test class
			return null;
		}
		SearchMatch sm = results.get(0);
		if ( results.size() > 1 ) {
			// TODO: is this even possible with the current search?
			// Disambiguate somehow to find a single match.
		}
		Object element = sm.getElement();
		IType associatedClass = null;
		if (element instanceof IType) {
			associatedClass = (IType) element;
		}
		if (null == associatedClass) {
			// element can be null for some reason or could represent something
			// else altogether
			// TODO: Create an associated test class
			return null;
		}
		// TODO: Is it a test class?  (Does it contain references to org.junit.*?)
		ICompilationUnit potentialCU = associatedClass.getCompilationUnit();
		if (null == potentialCU) {
			// getCompilationUnit could return null if it's a binary type
			// TODO: Create a new associated test class??
		}
		return potentialCU;
	}

	/**
	 * Uses the JDT {@link SearchEngine} to find a class matching the specified
	 * <i>fullyQualifiedClassName</i> in the Eclipse Workspace.
	 * @param fullyQualifiedClassName The name of the package and class to find. 
	 * @return A list of {@link SearchMatch} instances or <code>null</code> if
	 * there was a problem with the search. 
	 * (and thus probably a bug in this code!)
	 */
	List<SearchMatch> findClass(String fullyQualifiedClassName) {
		SearchPattern pattern = SearchPattern.createPattern (
				fullyQualifiedClassName, 
				IJavaSearchConstants.CLASS, 
				IJavaSearchConstants.DECLARATIONS, 
				SearchPattern.R_EXACT_MATCH );
		if (null == pattern) {
			// createPattern can return null if the pattern is ill-formed.
			// TODO: Determine if returning null is a wise thing to do here
			return null;
		}
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope ( );
		SearchEngine se = new SearchEngine ( );
		SimpleSearchRequestor requestor = new SimpleSearchRequestor ();
		try {
			se.search(
				pattern, 
				new SearchParticipant[] { 
						SearchEngine.getDefaultSearchParticipant()
				}, 
				scope, 
				requestor, 
				null);
		} catch (CoreException e) {
			// The search might fail because the classpath is incorrectly set
			// TODO: report this to the user
			return null;
		}
		
		List<SearchMatch> results = requestor.getResults();
		return results;
	}

	/**
	 * Records the current selection for possible future processing using
	 * {@link #run(IAction)}.
	 * 
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO: Handle non-outline (JavaEditor) invocations
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			_Members = ss.toList();
		}
		else {
			_Members = null;
			// TODO: Remove this once we no longer need it
			System.out.print ( "    -> Selection type not yet handled: " );
			System.out.println ( selection );
		}
		
		action.setEnabled( _Members != null );
	}

}
