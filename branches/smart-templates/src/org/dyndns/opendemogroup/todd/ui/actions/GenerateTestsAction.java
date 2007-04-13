package org.dyndns.opendemogroup.todd.ui.actions;

import java.text.MessageFormat;
import java.util.List;

import org.dyndns.opendemogroup.todd.SimpleSearchRequestor;
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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;

public class GenerateTestsAction extends ActionBase {

	protected String newLine;

	/**
	 * Executes the action, if possible, on the provided {@link IType} instance.
	 * @param potentialTypeToTest An {@link IType}, which may have originated
	 * from an {@link IStructuredSelection} or from detecting that the cursor
	 * was closest to a class declaration in the active editor.
	 */
	protected void run(IType potentialTypeToTest) {
		// TODO: The following TODOs might be better handled
		// transparently by generateTest if we invoke it on all of
		// eachClass' methods, in a loop.
		// TODO: determine if eachClass is abstract and react accordingly
		// TODO: write a testcase class
		// TODO: Consider invoking the current "New JUnit Test Case" wizard
		IMethod[] methods = null;
		try {
			methods = potentialTypeToTest.getMethods();
		} catch (JavaModelException jme) {
			// jme thrown if element does not exist or
			// if there's a horrible error during access
			return;
		}
		for ( IMethod each : methods ) {
			run ( each );
		}
	}

	/**
	 * Executes the action, if possible, on the provided {@link IMethod}
	 * instance.
	 * @param potentialMethodToTest An {@link IMethod}, which may have
	 * originated from an {@link IStructuredSelection}, from enumerating an
	 * {@link IType}'s methods or from detecting that the cursor was closest to
	 * a method declaration in the active editor.
	 */
	protected void run(IMethod potentialMethodToTest) {
		// TODO: Consider the scenario where someone tries to have a test
		// written for a constructor/destructor or other hard-to-call method
		int flags = 0;
		try {
			flags = potentialMethodToTest.getFlags ( );
		}
		catch ( JavaModelException jme ) {
			// jme thrown if element does not exist or
			// if there's a horrible error during access
			return;
		}
		// TODO: move this check to selectionChanged if possible
		if ( Flags.isPrivate(flags) ) {
			// can't call private member!
		}
		else if ( Flags.isProtected(flags) ) {
			// TODO: Also consider an abstract method
			// TODO: write a test for method only if test class subclasses us
		}
		else {
			generateTest ( potentialMethodToTest );
		}
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
		determineLineSeparator(testClass);
		String contents = generateTestMethod ( method, testClass );
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
	 * <i>line.separator</i> property is.  The result is stored in the protected
	 * field "newLine" for use by other methods in this class.
	 * @param targetClass The {@link IType} instance for which the line
	 * separator is to be determined. 
	 * text.
	 */
	void determineLineSeparator(IType targetClass) {
		IOpenable openableTestClass = targetClass.getOpenable();
		newLine = System.getProperty("line.separator");
		try {
			newLine = openableTestClass.findRecommendedLineSeparator();
		} catch (JavaModelException jme) {
			// jme may be thrown for various reasons which don't impact us,
			// and thus we ignore since we already have a reasonably sensible
			// default value for newLine
		}
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
		determineLineSeparator(testedType);
		String compilationUnitContents = 
			generateCompilationUnitContents(parentPackage);
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
			generateTestClassContents(className, testClassName);
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
	 * @param testClass The class in which this method is located.
	 * @return A string representing the method to be added to the test fixture
	 * that will exercise <i>methodToTest</i> after the user fills in a few
	 * TODOs.
	 */
	public String generateTestMethod(IMethod methodToTest, IType testClass) {
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
			"{2}" +
			"}{1}" +
			"";
		String body = generateTestMethodBody(methodToTest, testClass);
		String contents = 
			MessageFormat.format( testMethodTemplate, methodToTest.getElementName(), newLine, body );
		return contents;
	}

	/**
	 * Convenience method that takes care of setting up the test method stub's
	 * body.
	 * @param methodToTest
	 * @param testClass
	 * @return
	 */
	String generateTestMethodBody(IMethod methodToTest, IType testClass) {
		String className = testClass.getElementName();
		String methodName = methodToTest.getElementName();
		int flags = 0;
		try {
			flags = methodToTest.getFlags();
		} catch (JavaModelException jme) {
			// jme thrown if element does not exist (impossible) or if an
			// exception occurs while accessing its corresponding resource, so
			// let's ignore it for now and pretend it never happened.
		}
		// TODO: create an instance variable name based on the class name
		String instanceOrClass = 
			Flags.isStatic(flags) 
			? className 
			: determineInstanceVariableName(className);

		StringBuilder body = new StringBuilder ( );
		if ( !Flags.isStatic(flags) ) {
			// TODO: What happens if the class is abstract?  How about we
			// assume (detect) that there will be some subclasses of this test
			// class for all implementations (and even create one if none exist)
			// and delegate the initialization to subclasses by making ourselves
			// abstract as well? (we'd have to be careful not to fight the user
			// while doing this)
			appendFormat(body, "\t// TODO: Create an instance of the {2} class, using the shortest constructor available {1}", "", newLine, className );
		}
		String bodyTemplate = 
			"\t// TODO: invoke {2}.{0} and assert properties of its effects/output{1}" +
			"\tfail ( \"Test not yet written\" ); {1}" +
			"";
		appendFormat(body, bodyTemplate, methodName, newLine, instanceOrClass);
		return body.toString();
	}
	
	static String determineInstanceVariableName ( String className ) {
		// TODO: Investigate the use of the 
		// org.eclipse.jdt.internal.core.InternalNamingConventions
		// class' suggestLocalVariableNames method, since it has support for
		// exclusions (to prevent collisions), project pre- and suf- fixes, etc.

		String result = "instance"; // default value

		char firstCharacter = className.charAt(0);
		// if className starts with an uppercase letter...
		if ( Character.isUpperCase( firstCharacter ) ) {
			// lowercase it and append it to the rest
			result = Character.toLowerCase(firstCharacter) 
					+ className.substring(1);
			// TODO: if that name is already taken and there's another
			// uppercase letter in the className, try to form a variable name
			// using such successive "words" until all remaining splits have
			// been exhausted.
		}

		return result;
	}

	/**
	 * Convenience method to emulate a method of the same name in .NET's
	 * StringBuilder class.
	 * @param target
	 * @param pattern
	 * @param arguments
	 */
	private static void appendFormat ( StringBuilder target, String pattern, Object... arguments ) {
		String result = MessageFormat.format(pattern, arguments);
		target.append(result);
	}
	
	/**
	 * Generates a string representation of a JUnit 4 test class, which will
	 * host tests for the class <i>className</i> and be called
	 * <i>testClassName</i>.
	 * @param className The name of the class to test.
	 * @param testClassName The name of the test class, which will contain tests
	 * for the class called <i>className</i>.
	 * @return A string representing a class declaration for JUnit testing
	 * purposes.
	 */
	public String generateTestClassContents(String className, String testClassName) {
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
	 * @return A string representing a compilation unit in which a test fixture
	 * class will be added.
	 */
	public String generateCompilationUnitContents(IPackageFragment parentPackage) {
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
