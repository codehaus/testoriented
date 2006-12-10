package org.dyndns.opendemogroup.todd.ui.actions;

import java.util.List;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class GenerateTestsAction implements IObjectActionDelegate {

	/**
	 * Target {@link IMember} instances to operate on.  In our case, it will be
	 * methods ({@link IMethod}) and classes ({@link IType}). 
	 */
	List<IMember> _Members = null;
	
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
			for ( IMember each : _Members ) {
				if (each instanceof IMethod) {
					IMethod eachMethod = (IMethod) each;
					// TODO: move this test to selectionChanged if possible
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
					if ( (flags & Flags.AccPrivate) != 0 ) {
						// can't call private member!
					}
					else if ( (flags & Flags.AccProtected) != 0) {
						// TODO: Also consider an abstract method
						// TODO: write a test for method only if test class subclasses us
					}
					else {
						// TODO: write a test for method
					}
				}
				else if (each instanceof IType) {
					// TODO: I can't seem to trigger on IType and instead I get
					// an instance of File when I think I'm on a Class...
					IType eachClass = (IType) each;
					// TODO: determine if eachClass is abstract and react accordingly
					// TODO: write a testcase class
					// TODO: Consider invoking the current "New JUnit Test Case" wizard
				}
			}
		}
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
