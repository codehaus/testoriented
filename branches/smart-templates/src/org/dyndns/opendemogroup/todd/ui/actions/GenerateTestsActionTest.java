package org.dyndns.opendemogroup.todd.ui.actions;

import static org.junit.Assert.*;

import java.text.MessageFormat;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.junit.Before;
import org.junit.Test;

/**
 * A class to test the class
 * {@link org.dyndns.opendemogroup.todd.ui.actions.GenerateTestsAction}.
 */
public class GenerateTestsActionTest extends GenerateTestsAction {

	private static final String QJAVA_LANG_STRING = "Qjava.lang.String;";
	private static final String QSTRING = "QString;";
	private static final String LJAVA_LANG_STRING = "Ljava.lang.String;";

	@Before
	public void initializeNewLine ( ) {
		newLine = System.getProperty("line.separator");
	}

	/**
	 * A slightly lame test of 
	 * {@link GenerateTestsAction#generateTestMethod(IMethod,String)}
	 * that exercises the typical use.
	 */
	@Test
	public void generateTestMethod_Typical ( ) {
		TestingClass tc = new TestingClass ( );
		tc.setElementName("Unformatter");

		TestingMethod methodToTest = new TestingMethod ();
		methodToTest.setElementName("Unformat");
		tc.addMethod(methodToTest);

		TestingMethod constructor = new TestingMethod ( );
		constructor.setConstructor(true);
		tc.addMethod(constructor);

		String actual = generateTestMethod(methodToTest, tc);
		String testMethodTemplate =
			"{0}" +
			"/**{0}" +
			" * Tests the <i>Unformat</i> method with {0}" +
			" * TODO: write about scenario{0}" +
			" */{0}" +
			"@Test public void Unformat_TODO ( ) '{' {0}" +
			"\t// TODO: prelude (  );{0}" +
			"\t// TODO: invoke unformatter.Unformat and assert properties of its effects/output{0}" +
			"\tfail ( \"Test not yet written\" ); {0}" +
			"}{0}" +
			"";
		String expected = 
			MessageFormat.format( testMethodTemplate, newLine );
		assertEquals(expected, actual);
	}

	/**
	 * Tests the <i>determineInstanceVariableName</i> method with 
	 * a typical use of a class name that starts with an uppercase letter.
	 */
	@Test public void determineInstanceVariableName_UpperCaseFirstLetter ( ) { 
		String actual = 
			GenerateTestsAction.determineInstanceVariableName("StringBuilder");
		assertEquals("stringBuilder", actual);
	}

	/**
	 * Tests the <i>determineInstanceVariableName</i> method with 
	 * a typical use of a class name that starts with a lowercase letter.
	 */
	@Test public void determineInstanceVariableName_lowerCaseFirstLetter ( ) { 
		String actual = 
			GenerateTestsAction.determineInstanceVariableName("stringBuilder");
		assertEquals("instance", actual);
	}

	/**
	 * Tests the <i>determineInitializationForType</i> method with 
	 * a type of char.
	 */
	@Test public void determineInitializationForType_Character ( ) { 
		String actual = determineInitializationForType ( "C" );
		assertEquals("'x'", actual); 
	}

	/**
	 * Tests the <i>determineInitializationForType</i> method with 
	 * the sample signature <code>[I</code>.
	 */
	@Test public void determineInitializationForType_ArrayOfInt ( ) {
		String actual = determineInitializationForType ( "[I" );
		assertEquals("new int[] { 0 }", actual);
	}

	/**
	 * Tests the <i>determineInitializationForType</i> method with 
	 * the sample signature <code>[[I</code>.
	 */
	@Test public void determineInitializationForType_ArrayOfArrayOfInt ( ) {
		String actual = determineInitializationForType ( "[[I" );
		assertEquals("new int[][] { { 0 } }", actual);
	}

	/**
	 * Tests the <i>determineInitializationForType</i> method with 
	 * a resolved string, written as <code>Ljava.lang.String;</code>.
	 */
	@Test public void determineInitializationForType_ResolvedJavaLangString ( ) {
		String actual = determineInitializationForType(LJAVA_LANG_STRING);
		assertEquals("\"TODO\"", actual);
	}

	/**
	 * Tests the <i>determineInitializationForType</i> method with 
	 * an unresolved string, written as <code>QString;</code>.
	 */
	@Test public void determineInitializationForType_UnresolvedString ( ) {
		String actual = determineInitializationForType(QSTRING);
		assertEquals("\"TODO\"", actual);
	}

	/**
	 * Tests the <i>determineInitializationForType</i> method with 
	 * an unresolved string, written as <code>Qjava.lang.String;</code>.
	 */
	@Test public void determineInitializationForType_UnresolvedJavaLangString ( ) {
		String actual = determineInitializationForType(QJAVA_LANG_STRING);
		assertEquals("\"TODO\"", actual);
	}

	/**
	 * Tests the <i>determineDeclarationForType</i> method with 
	 * the sample signature <code>[[I</code>.
	 */
	@Test public void determineDeclarationForType_ArrayOfArrayOfInt ( ) { 
		String actual = determineDeclarationForType ( "[[I" );
		assertEquals("int[][]", actual); 
	}

	/**
	 * Tests the <i>determineDeclarationForType</i> method with 
	 * a resolved string, written as <code>Ljava.lang.String;</code>.
	 */
	@Test public void determineDeclarationForType_ResolvedJavaLangString ( ) { 
		String actual = determineDeclarationForType(LJAVA_LANG_STRING);
		assertEquals("java.lang.String", actual);
	}

	/**
	 * Tests the <i>determineDeclarationForType</i> method with 
	 * an unresolved string, written as <code>QString;</code>.
	 */
	@Test public void determineDeclarationForType_UnresolvedString ( ) { 
		String actual = determineDeclarationForType(QSTRING);
		assertEquals("String", actual);
	}

	/**
	 * Tests the <i>determineDeclarationForType</i> method with 
	 * an unresolved string, written as <code>Qjava.lang.String;</code>.
	 */
	@Test public void determineDeclarationForType_UnresolvedJavaLangString ( ) { 
		String actual = determineDeclarationForType(QJAVA_LANG_STRING);
		assertEquals("java.lang.String", actual);
	}

	/**
	 * Tests the <i>determinePreferredConstructor</i> method with 
	 * a typical scenario put together with lots of mocking.
	 */
	@Test public void determinePreferredConstructor_Typical ( ) {
		TestingClass tc = new TestingClass ( );
		TestingMethod defaultConstructor = new TestingMethod ( );
		defaultConstructor.setConstructor(true);
		tc.addMethod(defaultConstructor);
		
		TestingMethod parameterizedConstructor = createEatMeatMethod();
		parameterizedConstructor.setConstructor(true);
		tc.addMethod(parameterizedConstructor);

		IMethod actual = determinePreferredConstructor ( tc );
		assertEquals(defaultConstructor, actual); 
	}

	/**
	 * Tests the <i>determinePreferredConstructor</i> method with 
	 * the non-default constructor listed first.
	 */
	@Test public void determinePreferredConstructor_LongerFirst ( ) {
		TestingClass tc = new TestingClass ( );

		TestingMethod parameterizedConstructor = createEatMeatMethod();
		parameterizedConstructor.setConstructor(true);
		tc.addMethod(parameterizedConstructor);

		TestingMethod defaultConstructor = new TestingMethod ( );
		defaultConstructor.setConstructor(true);
		tc.addMethod(defaultConstructor);

		IMethod actual = determinePreferredConstructor ( tc );
		assertEquals(defaultConstructor, actual); 
	}

	/**
	 * Tests the <i>determinePreferredConstructor</i> method with 
	 * the default constructor as inaccessible.
	 */
	@Test public void determinePreferredConstructor_InaccessibleDefaultConstructor ( ) {
		TestingClass tc = new TestingClass ( );

		TestingMethod parameterizedConstructor = createEatMeatMethod();
		parameterizedConstructor.setConstructor(true);
		tc.addMethod(parameterizedConstructor);

		TestingMethod defaultConstructor = new TestingMethod ( );
		defaultConstructor.setConstructor(true);
		defaultConstructor.setFlags(Flags.AccPrivate);
		tc.addMethod(defaultConstructor);

		IMethod actual = determinePreferredConstructor ( tc );
		assertEquals(parameterizedConstructor, actual); 
	}

	/**
	 * Convenience method that creates a special instance of the
	 * TestingMethod class and initializes it accordingly.
	 * @return An instance of the <i>TestingMethod</i> class representing
	 * the following method:
	 * <code>public void eat ( String meat );</code> 
	 */
	private TestingMethod createEatMeatMethod() {
		TestingMethod result = new TestingMethod ( );
		result.setElementName("eat");
		result.addParameter("meat", "QString;");
		return result;
	}

	/**
	 * Tests the <i>generateCallStub</i> method with 
	 * a simple, one-argument method call.
	 */
	@Test public void generateCallStub_OneArgumentMethod ( ) { 
		TestingMethod tm = createEatMeatMethod();
		String expectedTemplate = 
			"\tString meat = \"TODO\";{0}" + 
			"\t// TODO: prelude ( meat );{0}";
		String expected = 
			MessageFormat.format( expectedTemplate, newLine );
		String actual = generateCallStub ( tm );
		assertEquals(expected, actual); 
	}

	/**
	 * Tests the <i>generateCallStub</i> method with 
	 * a simple, two-argument method call.
	 */
	@Test public void generateCallStub_TwoArgumentMethod ( ) {
		// it's really an extension/overload of the usual method
		TestingMethod tm = createEatMeatMethod();
		tm.addParameter("veggies", "Z"); // boolean
		String expectedTemplate = 
			"\tString meat = \"TODO\";{0}" + 
			"\tboolean veggies = false;{0}" + 
			"\t// TODO: prelude ( meat, veggies );{0}";
		String expected = 
			MessageFormat.format( expectedTemplate, newLine );
		String actual = generateCallStub ( tm );
		assertEquals(expected, actual); 
	}

	/**
	 * Tests the <i>generateCallStub</i> method with 
	 * a parameterized constructor.
	 */
	@Test public void generateCallStub_Constructor ( ) {
		// it's really an extension/overload of the usual method
		TestingMethod tm = createEatMeatMethod();
		tm.addParameter("veggies", "Z"); // boolean
		tm.addParameter("numberOfDesserts", "I"); // int
		tm.setConstructor(true);
		String expectedTemplate = 
			"\tString meat = \"TODO\";{0}" + 
			"\tboolean veggies = false;{0}" + 
			"\tint numberOfDesserts = 0;{0}" + 
			"\t// TODO: prelude ( meat, veggies, numberOfDesserts );{0}";
		String expected = 
			MessageFormat.format( expectedTemplate, newLine );
		String actual = generateCallStub ( tm );
		assertEquals(expected, actual); 
	}
}
