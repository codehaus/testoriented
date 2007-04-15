package org.dyndns.opendemogroup.todd.ui.actions;

import static org.junit.Assert.*;

import java.text.MessageFormat;

import org.junit.Before;
import org.junit.Test;

/**
 * A class to test the class
 * {@link org.dyndns.opendemogroup.todd.ui.actions.GenerateTestsAction}.
 */
public class GenerateTestsActionTest extends GenerateTestsAction {

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

}
