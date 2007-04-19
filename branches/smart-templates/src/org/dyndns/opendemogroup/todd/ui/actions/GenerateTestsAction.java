package org.dyndns.opendemogroup.todd.ui.actions;

import java.text.MessageFormat;
import java.util.Hashtable;
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
import org.eclipse.jdt.core.Signature;
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
	 * Represents a mapping of single-character primitive type name
	 * representations (as the TypeSignature) and sensible values for defaults
	 * when initializing them. 
	 */
	private static Hashtable<Character, String> simpleTypeDefaultValues = null;
	
	/**
	 * Represents a mapping of type names and sensible values for defaults
	 * when initializing them. 
	 */
	private static Hashtable<String, String> complexTypeDefaultValues = null;

	/**
	 * Represents a mapping of single-character primitive type name
	 * representations and their usual string representations in source code. 
	 */
	private static Hashtable<Character, String> simpleTypeByName = null;

	public GenerateTestsAction ( ) {
		super ( );
		synchronized (GenerateTestsAction.class) {
			// TODO: Eventually make these defaults configurable
			if (null == simpleTypeDefaultValues) {
				simpleTypeDefaultValues = new Hashtable<Character, String>();
				simpleTypeDefaultValues.put(Signature.C_BYTE, "0");
				simpleTypeDefaultValues.put(Signature.C_CHAR, "'x'");
				simpleTypeDefaultValues.put(Signature.C_DOUBLE, "0.0");
				simpleTypeDefaultValues.put(Signature.C_FLOAT, "0.0f");
				simpleTypeDefaultValues.put(Signature.C_INT, "0");
				simpleTypeDefaultValues.put(Signature.C_LONG, "0L");
				simpleTypeDefaultValues.put(Signature.C_SHORT, "0");
				simpleTypeDefaultValues.put(Signature.C_VOID, "null");
				simpleTypeDefaultValues.put(Signature.C_BOOLEAN, "false");
			}

			// TODO: Eventually make these defaults configurable
			if (null == complexTypeDefaultValues) {
				complexTypeDefaultValues = new Hashtable<String, String>();
				final String defaultString = "\"TODO\"";
				complexTypeDefaultValues.put("java.lang.String", defaultString);
				complexTypeDefaultValues.put("String", defaultString);
			}

			if (null == simpleTypeByName) {
				simpleTypeByName = new Hashtable<Character, String>();
				simpleTypeByName.put(Signature.C_BYTE, "byte");
				simpleTypeByName.put(Signature.C_CHAR, "char");
				simpleTypeByName.put(Signature.C_DOUBLE, "double");
				simpleTypeByName.put(Signature.C_FLOAT, "float");
				simpleTypeByName.put(Signature.C_INT, "int");
				simpleTypeByName.put(Signature.C_LONG, "long");
				simpleTypeByName.put(Signature.C_SHORT, "short");
				simpleTypeByName.put(Signature.C_VOID, "void");
				simpleTypeByName.put(Signature.C_BOOLEAN, "boolean");
			}
}		
	}
	
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
		// TODO: Investigate using Util.getLineSeparator instead of
		// (or in addition to) this code...
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
		int methodFlags = 0;
		String returnType = "V";
		try {
			methodFlags = methodToTest.getFlags();
			returnType = methodToTest.getReturnType();
		} catch (JavaModelException jme) {
			// jme thrown if element does not exist (impossible) or if an
			// exception occurs while accessing its corresponding resource, so
			// let's ignore it for now and pretend it never happened, since we
			// have reasonable defaults
		}

		StringBuilder body = new StringBuilder ( );
		if ( !Flags.isStatic(methodFlags) ) {
			IMethod constructor = determinePreferredConstructor(testClass);
			if (constructor != null) {
				String construction = generateCallStub(constructor);
				body.append(construction);
			}
		}
		
		// TODO: I can actually find out how many variables/parameters there
		// are, (if any!) so this message could be much more accurate...
		body.append("\tfail ( \"TODO: initialize variable(s)");
		if (returnType != "V") {
			body.append(" and expected value");
		}
		body.append("\" );");
		body.append(newLine);
		// TODO: Use something like:
		// org.eclipse.jdt.core.NamingConventions.suggestLocalVariableNames
		// ...if a parameter's name is not available or conflicts with an
		// existing name in the current scope.
		String methodCall = generateCallStub(methodToTest);
		body.append(methodCall);
		if ( returnType != "V" ) {
			String declaration = determineDeclarationForType(returnType);
			String initialization = determineInitializationForType(returnType);
			appendFormat(body, "\t{0} expected = {1};{2}", 
					declaration, initialization, newLine);
			body.append ( "\tassertEquals ( expected, actual );" );
			body.append ( newLine );
		}
		return body.toString();
	}

	/**
	 * <p>
	 * Given a method, will generate some code that declares and initializes
	 * variables corresponding to the method's parameters (if any) and then
	 * calls the method.
	 * </p>
	 * <p>
	 * This will work with methods that represent constructors, too.  In that
	 * case, a new instance of the type in which the constructor method is found
	 * will be created.
	 * </p>
	 * @param method The method for which to generate code that calls it.
	 * @return A string representation of the code necessary to call the method
	 * and any supporting code necessary to initialize variables matching the
	 * method's parameters.
	 */
	String generateCallStub(IMethod method) {
		// TODO: if the method returns something, allow the user to provide
		// a name for that variable.
		StringBuilder sb = new StringBuilder ( );
		int numberOfParameters = method.getNumberOfParameters();
		String methodCall = null;
		try {
			String[] parameterNames = method.getParameterNames();
			String[] parameterTypes = method.getParameterTypes();
			// generate parameter variable initialization(s)
			for (int i = 0; i < numberOfParameters; i++) {
				String parameterName = parameterNames[i];
				String parameterType = parameterTypes[i];
				String declaration = determineDeclarationForType(parameterType);
				String initialization = determineInitializationForType(parameterType);
				appendFormat(sb, 
					"\t{0} {1} = {2};", 
					declaration, parameterName, initialization);
				sb.append(newLine);
			}

			sb.append("\t");
			IType declaringType = method.getDeclaringType();
			String className = declaringType.getElementName();
			String returnType = method.getReturnType();
			String instanceOrClass = 
				Flags.isStatic(method.getFlags()) 
				? className 
				: determineInstanceVariableName(className);
			String methodName = method.getElementName();
			// assign result if necessary
			if (method.isConstructor()) {
				// generate "className instanceOrClass = "
				appendFormat(sb, "{0} {1} = ", className, instanceOrClass);
			}
			// if method returns something...
			else if (returnType != "V") {
				// generate "returnType actual = "
				String declaration = determineDeclarationForType(returnType);
				appendFormat(sb, "{0} {1} = ", declaration, "actual");
			}

			// Make the call!
			if (method.isConstructor()) {
				// "new className"
				appendFormat(sb, "new {0}", className);
			}
			else {
				// "instanceOrClass.methodName"
				appendFormat(sb, "{0}.{1}", instanceOrClass, methodName);
			}

			// generate argument/parameter list
			sb.append(" ( ");
			for (int i = 0; i < numberOfParameters; i++) {
				String parameterName = parameterNames[i];
				if ( i > 0 ) {
					sb.append(", ");
				}
				sb.append(parameterName);
			}
			sb.append(" );");
			sb.append(newLine);
			methodCall = sb.toString();
		} catch (JavaModelException e) {
			// jme thrown if element does not exist (impossible) or if an
			// exception occurs while accessing its corresponding resource,
			// which means we weren't able to generate a method call
		}		
		return methodCall;
	}

	/**								
	 * Scans the provided <i>testClass</i>'s methods to find accessible
	 * constructors and then narrows the list to the most desirable/preferable. 
	 * @param testClass The IType instance for which to scan the methods.
	 * @return An IMethod from the IType which represents the best constructor
	 * to call, if one is available; null otherwise.
	 */
	static IMethod determinePreferredConstructor(IType testClass) {
		IMethod result = null;
		IMethod[] methods = null;
		// TODO: What if all constructors are private/protected?  That might
		// mean we have a singleton with an "Instance" or other factory
		// method that creates instances for us.
		// TODO: What if the best constructor is recursive? For example:
		// Node ( Node parent )
		// ...although parent could probably be null in this case...
		// TODO: What happens if the class is abstract?  How about we
		// assume (detect) that there will be some subclasses of this test
		// class for all implementations (and even create one if none exist)
		// and delegate the initialization to subclasses by making ourselves
		// abstract as well? (we'd have to be careful not to fight the user
		// while doing this)
		// TODO: Provide a means for configuring an override for user-preferred
		// constructors, which could be recognizable by an annotation.
		try {
			methods = testClass.getMethods();
			if ( methods != null && methods.length > 0 ) {
				// let's do this like a contest: the better of every pair of
				// constructors is kept, unless it's the first one we find...
				for (int i = 0; i < methods.length; i++) {
					IMethod method = methods[i];
					int flags = method.getFlags();
					// non-static, non-private and non-protected constructors
					if ( method.isConstructor() 
							&& !Flags.isStatic(flags) 
							&& !Flags.isPrivate(flags)
							&& !Flags.isProtected(flags) ) {
						if (null == result) {
							result = method;
						}
						else {
							// is _method_ better than _result_?
							// TODO: Improve the meaning of "better", because in
							// this case, there is "another kind of better". For
							// example, maybe we should avoid recursive
							// constructors, deprecated ones, etc.
							if ( method.getNumberOfParameters() 
									< result.getNumberOfParameters() ) {
								result = method;
							}
						}
					}
				}
			}
		} catch (JavaModelException jme) {
			// jme thrown if element does not exist (impossible) or if an
			// exception occurs while accessing its corresponding resource.
			// I'll just go ahead and ignore this for now...
		}

		return result;
	}

	/**
	 * Given a string representation of a type signature, parses it and attempts
	 * to reconstruct what the declaration would have looked like in source
	 * code before it was converted into a signature.
	 * @param typeSignature A string representation of a type's signature.
	 * @return A string representation of the source code that likely produced
	 * the signature.
	 */
	String determineDeclarationForType ( String typeSignature ) {
		char firstCharacter = typeSignature.charAt(0);
		String rest = typeSignature.substring(1);
		String result = "null";
		switch ( firstCharacter ) {
		case Signature.C_TYPE_VARIABLE:
			// TODO: implement this possibility
			break;
		case Signature.C_ARRAY:
			result = determineDeclarationForType(rest) + "[]";
			break;
		case Signature.C_CAPTURE:
			// TODO: implement this possibility
			break;
		case Signature.C_RESOLVED:
		case Signature.C_UNRESOLVED:
			// TODO: add support for common simplifications, such as:
			// java.lang.String -> String
			// java.lang.Object -> Object
			if ( rest.contains( "<" ) ) {
				// TODO: Add support for type arguments
			}
			else {
				// just grab rest minus the last character, which should be ';'
				if (rest.endsWith(";")) {
					result = rest.substring(0, rest.length() - 1);
				}				
			}
			break;
		default:	// all others - they are simple types
			if ( simpleTypeByName.containsKey(firstCharacter) ) {
				result = simpleTypeByName.get(firstCharacter);
			}
			break;
		}
		return result;
	}
	
	/**
	 * When calling a method or a constructor, arguments must be provided values
	 * that match their type.  This method decodes the provided
	 * <i>typeSignature</i> and returns the string representation of a sensible
	 * default value with which to initialize a variable/parameter of that type.
	 * @param typeSignature
	 * @return
	 */
	String determineInitializationForType ( String typeSignature ) {
		char firstCharacter = typeSignature.charAt(0);
		String rest = typeSignature.substring(1);
		String result = "null";
		String typeName = null;
		String defaultValue = null;
		switch ( firstCharacter ) {
		case Signature.C_TYPE_VARIABLE:
			// TODO: implement this possibility
			break;
		case Signature.C_ARRAY:
			result = determineInitializationForArray(typeSignature, rest);
			break;
		case Signature.C_CAPTURE:
			// TODO: implement this possibility
			break;
		case Signature.C_RESOLVED:
		case Signature.C_UNRESOLVED:
			if ( rest.contains( "<" ) ) {
				// TODO: Add support for type arguments
			}
			else {
				// TODO: Handle nested types
				// (such as java.util.Map<K,V>.Entry<K,V>)

				// just grab rest minus the last character, which should be ';'
				if (rest.endsWith(";")) {
					typeName = rest.substring(0, rest.length() - 1);
				}
				// then see if there's a pre-defined default
				if ( complexTypeDefaultValues.containsKey(typeName) ) {
					result = complexTypeDefaultValues.get(typeName);
				}
				else {
					// TODO: call a constructor if possible
				}
			}
			break;
		default:	// all others - they are simple types
			if ( simpleTypeDefaultValues.containsKey(firstCharacter) ) {
				result = simpleTypeDefaultValues.get(firstCharacter);
			}
			break;
		}
		return result;
	}

	/**
	 * Given that we know the <i>typeSignature</i> to represent an array,
	 * construct a string representing the initialization of the array type
	 * represented by the signature.
	 * @param typeSignature
	 * @param rest The <i>typeSignature</i> minus its first character.
	 * @return A string representing the initialization of the type represented
	 * by <i>typeSignature</i>.
	 */
	private String determineInitializationForArray(String typeSignature, String rest) {
		String result;
		String typeName = determineDeclarationForType ( typeSignature );
		int braceCount = 1;
		int index;
		// TODO: In theory, the grammar could allow the sequence "[![",
		// but this loop won't handle it.  Although, is that even a valid
		// signature???  What should happen if we hit an invalid signature??
		for (index = 0; index < rest.length ( ); index++) {
			char c = rest.charAt(index);
			if ( Signature.C_ARRAY == c ) {
				braceCount++;
			}
			else {
				break;
			}
		}
		// rest is whatever is after all those '[' (which index points to) 
		rest = rest.substring(index);
		String defaultValue = determineInitializationForType(rest);
		StringBuffer sb = new StringBuffer ( );
		sb.append( "new " );
		sb.append(typeName);
		sb.append( " " );
		for ( int c = 0; c < braceCount; c++ ) {
			sb.append("{ ");
		}
		sb.append(defaultValue);
		for ( int c = 0; c < braceCount; c++ ) {
			sb.append(" }");
		}
		result = sb.toString();
		return result;
	}

	/**
	 * Given the name of a class, attempts to create a suitable name for an
	 * instance variable in which an instance of said class could be assigned.
	 * @param className The name of a class.
	 * @return The name of a variable.
	 */
	static String determineInstanceVariableName ( String className ) {
		// TODO: Investigate the use of the 
		// org.eclipse.jdt.internal.core.InternalNamingConventions
		// class' suggestLocalVariableNames method, since it has support for
		// exclusions (to prevent collisions), project pre- and suf- fixes, etc.
		
		// TODO: Handle nested classes (such as Map<K,V>.Entry<K,V>) and
		// qualified names (such as java.lang.String)

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
