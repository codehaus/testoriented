package org.dyndns.opendemogroup.todd.ui.actions;

import static org.junit.Assert.*;

import java.text.MessageFormat;

import org.junit.Test;

/**
 * A class to test the class
 * {@link org.dyndns.opendemogroup.todd.ui.actions.GenerateTestsAction}.
 */
public class GenerateTestsActionTest extends GenerateTestsAction {

	private static final String NEWLINE = System.getProperty("line.separator");

	/**
	 * A slightly lame test of 
	 * {@link GenerateTestsAction#generateTestMethod(IMethod,String)}
	 * that exercises the typical use.
	 */
	@Test
	public void generateTestMethod_Typical ( ) {
		TestingMethod tm = new TestingMethod ();
		tm.setElementName("Unformat");
		String actual = generateTestMethod(tm, NEWLINE, null);
		String testMethodTemplate =
			"{0}" +
			"/**{0}" +
			" * Tests the <i>Unformat</i> method with {0}" +
			" * TODO: write about scenario{0}" +
			" */{0}" +
			"@Test public void Unformat_TODO ( ) '{' {0}" +
			"\t// TODO: invoke Unformat and assert properties of its effects/output{0}" +
			"\tfail ( \"Test not yet written\" ); {0}" +
			"}{0}" +
			"";
		String expected = 
			MessageFormat.format( testMethodTemplate, NEWLINE );
		assertEquals(expected, actual);
	}

}
