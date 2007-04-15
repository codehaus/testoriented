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
		TestingMethod tm = new TestingMethod ();
		tm.setElementName("Unformat");
		TestingClass tc = new TestingClass ( );
		tc.setElementName("Unformatter");
		String actual = generateTestMethod(tm, tc);
		String testMethodTemplate =
			"{0}" +
			"/**{0}" +
			" * Tests the <i>Unformat</i> method with {0}" +
			" * TODO: write about scenario{0}" +
			" */{0}" +
			"@Test public void Unformat_TODO ( ) '{' {0}" +
			"\t// TODO: Create an instance of the Unformatter class, using the shortest constructor available {0}" +
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
	 * the sample signature <c>[I</c>.
	 */
	@Test public void determineInitializationForType_ArrayOfInt ( ) {
		String actual = determineInitializationForType ( "[I" );
		assertEquals("new int[] { 0 }", actual);
	}

	/**
	 * Tests the <i>determineInitializationForType</i> method with 
	 * the sample signature <c>[[I</c>.
	 */
	@Test public void determineInitializationForType_ArrayOfArrayOfInt ( ) {
		String actual = determineInitializationForType ( "[[I" );
		assertEquals("new int[][] { { 0 } }", actual);
	}

	/**
	 * Tests the <i>determineInitializationForType</i> method with 
	 * a resolved string, written as <c>Ljava.lang.String;</c>.
	 */
	@Test public void determineInitializationForType_ResolvedJavaLangString ( ) {
		String actual = determineInitializationForType(LJAVA_LANG_STRING);
		assertEquals("\"TODO\"", actual);
	}

	/**
	 * Tests the <i>determineInitializationForType</i> method with 
	 * an unresolved string, written as <c>QString;</c>.
	 */
	@Test public void determineInitializationForType_UnresolvedString ( ) {
		String actual = determineInitializationForType(QSTRING);
		assertEquals("\"TODO\"", actual);
	}

	/**
	 * Tests the <i>determineInitializationForType</i> method with 
	 * an unresolved string, written as <c>Qjava.lang.String;</c>.
	 */
	@Test public void determineInitializationForType_UnresolvedJavaLangString ( ) {
		String actual = determineInitializationForType(QJAVA_LANG_STRING);
		assertEquals("\"TODO\"", actual);
	}

	/**
	 * Tests the <i>reconstructTypeSignature</i> method with 
	 * the sample signature <c>[[I</c>.
	 */
	@Test public void reconstructTypeSignature_ArrayOfArrayOfInt ( ) { 
		String actual = reconstructTypeSignature ( "[[I" );
		assertEquals("int[][]", actual); 
	}

	/**
	 * Tests the <i>reconstructTypeSignature</i> method with 
	 * a resolved string, written as <c>Ljava.lang.String;</c>.
	 */
	@Test public void reconstructTypeSignature_ResolvedJavaLangString ( ) { 
		String actual = reconstructTypeSignature(LJAVA_LANG_STRING);
		assertEquals("java.lang.String", actual);
	}

	/**
	 * Tests the <i>reconstructTypeSignature</i> method with 
	 * an unresolved string, written as <c>QString;</c>.
	 */
	@Test public void reconstructTypeSignature_UnresolvedString ( ) { 
		String actual = reconstructTypeSignature(QSTRING);
		assertEquals("String", actual);
	}

	/**
	 * Tests the <i>reconstructTypeSignature</i> method with 
	 * an unresolved string, written as <c>Qjava.lang.String;</c>.
	 */
	@Test public void reconstructTypeSignature_UnresolvedJavaLangString ( ) { 
		String actual = reconstructTypeSignature(QJAVA_LANG_STRING);
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
		
		TestingMethod parameterizedConstructor = new TestingMethod ( );
		parameterizedConstructor.setConstructor(true);
		parameterizedConstructor.addParameter("Meat", "QString;");
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

		TestingMethod parameterizedConstructor = new TestingMethod ( );
		parameterizedConstructor.setConstructor(true);
		parameterizedConstructor.addParameter("Meat", "QString;");
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

		TestingMethod parameterizedConstructor = new TestingMethod ( );
		parameterizedConstructor.setConstructor(true);
		parameterizedConstructor.addParameter("Meat", "QString;");
		tc.addMethod(parameterizedConstructor);

		TestingMethod defaultConstructor = new TestingMethod ( );
		defaultConstructor.setConstructor(true);
		defaultConstructor.setFlags(Flags.AccPrivate);
		tc.addMethod(defaultConstructor);

		IMethod actual = determinePreferredConstructor ( tc );
		assertEquals(parameterizedConstructor, actual); 
	}
}
