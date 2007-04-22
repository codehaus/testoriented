package org.dyndns.opendemogroup.todd.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public abstract class ActionBase 
	implements IObjectActionDelegate, IEditorActionDelegate  {

	/**
	 * Target instances to operate on.  In our case, it will most likely be
	 * methods ({@link IMethod}) and classes ({@link IFile} and {@link IType}). 
	 */
	private List _Members;

	public ActionBase() {
		super ( );
		_Members = null;
	}

	/**
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		// TODO Auto-generated method stub
	
	}

	/**
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub
	
	}

	/**
	 * Records the current selection (if it's structured) for possible future
	 * processing using {@link #run(IAction)}.
	 * 
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		_Members = null;
		// TODO: Review comment #3 of Eclipse bug 118543:
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=118543#c3
		// ...to see if we should structure this code block differently.
		if (selection instanceof IStructuredSelection) {
			// we were invoked from the Outline or Package Explorer 
			IStructuredSelection ss = (IStructuredSelection) selection;
			// what to operate on is trivial to extract
			_Members = ss.toList();
		} else if (selection instanceof ITextSelection) {
			// we were invoked from the Java Editor

			// It doesn't appear that this event is fired reliably or
			// frequently enough for us to do anything (such as enable/disable)
			// and thus we simply do nothing so that #run(IAction), seeing that
			// _Members is null, will try to discover a useful selection and use
			// it.
		} else {
			// TODO: Remove this once we no longer need it
			System.out.print("    -> Selection type not yet handled: ");
			System.out.println(selection);
		}		
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (null == _Members) {
			// no selection from selectionChanged, try to guess one from the
			// active editor's cursor location
			IJavaElement element = getSelectedElement();
			if (element != null) {
				_Members = new ArrayList(1);
				_Members.add(element);
			}
		}
		
		if ( _Members != null ) {
			for ( Object each : _Members ) {
				if (each instanceof IMethod) {
					run((IMethod) each);
				}
				else if (each instanceof IFile) {
					IFile eachFile = (IFile) each;
					// TODO: determine the appropriate IType and simply call
					// #run(IType)
				}
				else if (each instanceof IType) {
					// TODO: I can't seem to trigger on IType and instead I get
					// an instance of File when I think I'm on a Class...
					// This may be because I defined an extension on IFile?!?!?
					run((IType) each);
				}
			}
			// TODO: Might need to have this happen in a "finally" block
			_Members = null;
		}
	}

	/**
	 * Returns the smallest element within the active editor's compilation unit
	 * that includes the current cursor location (that is, a method, field, 
	 * etc.), or <code>null</code> if there is no element other than the
	 * compilation unit itself at the cursor's current position, or if the
	 * current cursor position is not within the source range of this
	 * compilation unit.
	 * 
	 * @return The innermost Java element enclosing the given source position or
	 * <code>null</code> if none (excluding the compilation unit).
	 * 
	 * @see ICompilationUnit#getElementAt(int);
	 */
	IJavaElement getSelectedElement ( ) {
		// TODO: See if we can optimize the traversal by using the result of
		// IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
		// TODO: Many of the intermediary steps here could return null
		// for various reasons and thus in those cases so should we.
		IJavaElement element = null;
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow activeWorkbenchWindow = 
			workbench.getActiveWorkbenchWindow();
		IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		IEditorPart activeEditor = activePage.getActiveEditor();
		IEditorSite site = activeEditor.getEditorSite();
		ISelectionProvider provider = site.getSelectionProvider();
		ISelection selection = provider.getSelection();
		if (selection instanceof ITextSelection) {
			ITextSelection textSelection = (ITextSelection) selection;
			IEditorInput editorInput = activeEditor.getEditorInput();
			// editorInputJavaElement should be an ICompilationUnit
			IJavaElement editorInputJavaElement = 
				JavaUI.getEditorInputJavaElement(editorInput);
			if (editorInputJavaElement instanceof ICompilationUnit) {
				ICompilationUnit cu = (ICompilationUnit) editorInputJavaElement;
				try {
					element = 
						cu.getElementAt(textSelection.getOffset());
				} catch (JavaModelException jme) {
					// jme thrown if the cu does not exist (shouldn't happen)
					// or if something horrible happens while accessing its
					// resource
				}
			}
		}
		return element;
	}

	/**
	 * Executes the action, if possible, on the provided {@link IMethod}
	 * instance.
	 * @param targetMethod An {@link IMethod}, which may have
	 * originated from an {@link IStructuredSelection}, from enumerating an
	 * {@link IType}'s methods or from detecting that the cursor was closest to
	 * a method declaration in the active editor.
	 */
	protected abstract void run ( IMethod targetMethod );
	
	/**
	 * Executes the action, if possible, on the provided {@link IType} instance.
	 * @param targetType An {@link IType}, which may have originated
	 * from an {@link IStructuredSelection} or from detecting that the cursor
	 * was closest to a class declaration in the active editor.
	 */
	protected abstract void run(IType targetType);

}