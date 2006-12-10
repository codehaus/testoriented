package org.dyndns.opendemogroup.todd.ui.actions;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.dyndns.opendemogroup.todd.SimpleSearchRequestor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class GenerateTestsAction 
	implements IObjectActionDelegate, IEditorActionDelegate {

	/**
	 * Target instances to operate on.  In our case, it will most likely be
	 * methods ({@link IMethod}) and classes ({@link IFile} and {@link IType}). 
	 */
	List _Members = null;

	/**
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		// TODO: we may want to store the active editor for future optimization
		// Do nothing (on purpose).
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// Do nothing (on purpose).
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
	 * @see IActionDelegate#run(IAction)
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
					IMethod eachMethod = (IMethod) each;
					int flags = 0;
					try {
						flags = eachMethod.getFlags ( );
					}
					catch ( JavaModelException jme ) {
						// TODO: Find out when this is thrown and see if there's
						// a better reaction than simply stopping everything.
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
					// TODO: The following TODOs might be better handled
					// transparently by generateTest if we invoke it on all of
					// eachClass' methods, in a loop.
					// TODO: determine if eachClass is abstract and react accordingly
					// TODO: write a testcase class
					// TODO: Consider invoking the current "New JUnit Test Case" wizard
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
	 * Given an {@link IMethod} instance, will attempt to generate a JUnit test
	 * method for it in an appropriate associated test class.
	 * @param method The method for which a test is to be generated.
	 */
	void generateTest(IMethod method) {
		IType testClass = fetchAssociatedTestClass ( method );
		if (null == testClass) {
			// fetchTestClass (currently) may return nothing, thus we do nothing  
			// TODO: When fetchTestClass no longer technically returns null,
			// apologize to the user for not being able to generate the
			// test for them.
			return;
		}
		
		// TODO: Search for a spot to insert the new test method:
		// After last occurence of eachMethod.getName, or as the last method.
		// TODO: Consider scanning for special comments delineating test regions
		String newLine = determineLineSeparator(testClass);
		String contents = generateTestMethodContents ( method, newLine );
		// Open an editor for testClass, so the user can see the
		// newly-added method in context and then adjust it accordingly.
		ICompilationUnit cu = testClass.getCompilationUnit();
		// cu could be null if testClass is not declared in a compilation unit
		if (null == cu) {
			// TODO: Apologize to the user for not being able to generate the
			// test for them.
			return;
		}
		IEditorPart javaEditor = null;
		try {
			javaEditor = JavaUI.openInEditor ( cu );
		} catch (PartInitException pie) {
			// pie thrown "if the editor could not be initialized",
			// so fall through to the null check
		} catch (JavaModelException jme) {
			// jme may be thrown for various reasons which shouldn't really
			// happen in our case, so fall through to the null check
		}
		if (null == javaEditor) {
			// no editor means I'm not writing a test!
			// TODO: Apologize to the user for not being able to generate the
			// test for them.
			return;
		}
		IMethod testMethod;
		try {
			testMethod = testClass.createMethod(contents, null, true, null);
		} catch (JavaModelException jme) {
			// JME might be thrown for various reasons (see documentation)
			// TODO: we should report this to the user 
			return;
		}
		JavaUI.revealInEditor(javaEditor, (IJavaElement)testMethod);
	}

	/**
	 * Attempts to determine the line separator character(s) for the supplied
	 * <i>targetClass</i> with a default of whatever the platform's
	 * <i>line.separator</i> property is.
	 * @param targetClass The {@link IType} instance for which the line
	 * separator is to be determined. 
	 * @return A string representing the character(s) to use between lines of
	 * text.
	 */
	String determineLineSeparator(IType targetClass) {
		IOpenable openableTestClass = targetClass.getOpenable();
		String newLine = System.getProperty("line.separator");
		try {
			newLine = openableTestClass.findRecommendedLineSeparator();
		} catch (JavaModelException jme) {
			// jme may be thrown for various reasons which don't impact us,
			// and thus we ignore since we already have a reasonably sensible
			// default value for newLine
		}
		return newLine;
	}

	/**
	 * Attempts to find or create an associated test class for the class in
	 * which the specified <i>testedMethod</i> is found.
	 * @param testedMethod The {@link IMethod} instance for which to obtain an
	 * associated test class.
	 * @return An {@link ICompilationUnit} which represents the associated
	 * test class.
	 */
	IType fetchAssociatedTestClass(IMethod testedMethod) {
		// determine method's class and package names
		IType parentClass = testedMethod.getDeclaringType();
		if (null == parentClass) {
			// A method could be declared in a top-level type
			// TODO: Is test writing impossible, then?
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
	IType fetchAssociatedTestClass(IType testedType) {
		String className = testedType.getElementName();
		IPackageFragment parentPackage = testedType.getPackageFragment();
		String packageName = parentPackage.getElementName();
		
		// TODO: Parameterize (and centralize) the convention; old one follows  
		// Search for class with same name in package (packageName + ".test")
		// String associatedTestClassName = packageName + ".test." + className;
		
		// Search for class with name (className + "Test") in same package
		String associatedTestClassName = packageName + "." + className + "Test";
		List<SearchMatch> results = findClass(associatedTestClassName);
		// If none are found...
		IType associatedClass = null;
		if ( null == results || 0 == results.size() ) {
			associatedClass = createAssociatedTestClass ( testedType );
			return associatedClass;
		}
		SearchMatch sm = results.get(0);
		if ( results.size() > 1 ) {
			// TODO: is this even possible with the current search?
			// Disambiguate somehow to find a single match.
		}
		Object element = sm.getElement();
		if (element instanceof IType) {
			associatedClass = (IType) element;
			// TODO: Is it a test class? 
			// For example, does it contain references to org.junit.*?
		}
		if (null == associatedClass) {
			// element can be null for some reason or could represent something
			// else altogether
			associatedClass = createAssociatedTestClass ( testedType );
		}
		return associatedClass;
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
	 * Given an <code>IType</code> instance, will attempt to create another
	 * <code>IType</code> that represents a JUnit 4 test class intended to
	 * test <i>testedType</i>.
	 * @param testedType The {@link IType} for which a test fixture will be
	 * generated and added to the workspace.
	 * @return An <code>IType</code>, in its own {@link ICompilationUnit},
	 * ready to have methods added to it to test <i>testedType</i>.
	 */
	IType createAssociatedTestClass ( IType testedType ) {
		// TODO: Consider invoking the current "New JUnit Test Case" wizard,		
		// which might just be the methods JavaUI.createMainTypeDialog or
		// JavaUI.createTypeDialog
		String className = testedType.getElementName();
		IPackageFragment parentPackage = testedType.getPackageFragment();
		String newLine = determineLineSeparator(testedType);
		String compilationUnitContents = 
			generateCompilationUnitContents(parentPackage, newLine);
		// TODO: Parameterize the convention somehow (Strategy pattern?)
		String testClassName = className + "Test";
		ICompilationUnit associatedUnit = null;
		try {
			associatedUnit = 
				parentPackage.createCompilationUnit(
					testClassName + ".java", 
					compilationUnitContents, 
					false, 
					null);
		} catch (JavaModelException jme) {
			// jme thrown if name is not a valid CU name (can this even happen?)
			// or some other catastrophic error
			return null;
		}
		IType associatedClass = null;
		String classContents = 
			generateTestClassContents(className, testClassName, newLine);
		try {
			associatedClass = 
				associatedUnit.createType(
						classContents, 
						null, 
						true, 
						null);
		} catch (JavaModelException jme) {
			// jme thrown if sibling does not exist or is invalid (which is
			// impossible in our case because we give "null"), there's a naming
			// collision with an existing type (which should be impossible,
			// otherwise the detection would have found it -- or there's a
			// really weird package organization going on), the contents
			// is not a type declaration (a bug in this code) or some other
			// core error.
			return null;
		}
		return associatedClass;
	}

	/**
	 * Generates a string representation of a JUnit 4 test method that tests
	 * <i>methodToTest</i>.
	 * @param methodToTest The {@link IMethod} instance for which a test method
	 * will be generated.
	 * @param newLine The character or character sequence to use as a line
	 * separator in code.
	 * @return A string representing the method to be added to the test fixture
	 * that will exercise <i>methodToTest</i> after the user fills in a few
	 * TODOs.
	 */
	public static String generateTestMethodContents(IMethod methodToTest, String newLine) {
		String methodName = methodToTest.getElementName();
		// TODO: de-hardcode this method template for customization purposes
		// TODO: Also generate a call to the method under test with
		// auto-generated default values for its parameters.
		String testMethodTemplate =
			"{1}" +
			"/**{1}" +
			" * Tests the <i>{0}</i> method with {1}" +
			" * TODO: write about scenario{1}" +
			" */{1}" +
			"@Test public void {0}_TODO ( ) '{' {1}" +
			"\t// TODO: invoke {0} and assert properties of its effects/output{1}" +
			"\tfail ( \"Test not yet written\" ); {1}" +
			"}{1}" +
			"";
		String contents = 
			MessageFormat.format( testMethodTemplate, methodName, newLine );
		return contents;
	}

	/**
	 * Generates a string representation of a JUnit 4 test class, which will
	 * host tests for the class <i>className</i> and be called
	 * <i>testClassName</i>.
	 * @param className The name of the class to test.
	 * @param testClassName The name of the test class, which will contain tests
	 * for the class called <i>className</i>.
	 * @param newLine The character or character sequence to use as a line
	 * separator in code.
	 * @return A string representing a class declaration for JUnit testing
	 * purposes.
	 */
	public static String generateTestClassContents(String className, String testClassName, String newLine) {
		// TODO: In some cases, it may be desirable to have testClass derive
		// from class, so that protected methods can be exercised.
		// TODO: de-hardcode this class declaration template for customization
		// purposes, or at least initialize it from Eclipse's set of templates
		String classContentsTemplate = 
			"/**{0}" +
			" * A class to test the class {2}{0}" +
			" */{0}" +
			"public class {1} '{'{0}" +
			"\t{0}" +
			"}{0}" +
			"";
		String classContents = 
			MessageFormat.format(
					classContentsTemplate, 
					newLine, 
					testClassName, 
					className);
		return classContents;
	}

	/**
	 * Generates a string representation of a compilation unit which will host
	 * a JUnit 4 test class.
	 * @param parentPackage The {@link IPackageFragment} in which the class to
	 * be tested resides in.
	 * @param newLine The character or character sequence to use as a line
	 * separator in code.
	 * @return A string representing a compilation unit in which a test fixture
	 * class will be added.
	 */
	public static String generateCompilationUnitContents(IPackageFragment parentPackage, String newLine) {
		// TODO: de-hardcode this compilation unit template for customization
		// purposes, or at least initialize it from Eclipse's set of templates
		String compilationUnitTemplate = 
			"package {1};{0}" +
			"{0}" +
			"import static org.junit.Assert.*;{0}" +
			"import org.junit.Test;{0}" +
			"{0}" +
			"";
		String compilationUnitContents = 
			MessageFormat.format(
				compilationUnitTemplate, 
				newLine, 
				parentPackage.getElementName());
		return compilationUnitContents;
	}

}
