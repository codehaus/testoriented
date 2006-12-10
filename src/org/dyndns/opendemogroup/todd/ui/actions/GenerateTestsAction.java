package org.dyndns.opendemogroup.todd.ui.actions;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
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
			// fetchTestClass may return nothing, thus we do nothing  
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
		
		// TODO: search for class with same name in package (package + ".test")
		// TODO: If none are found, create one.
		// TODO: Is it a test class?  (Does it contain references to org.junit.*?)
		return null;
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
