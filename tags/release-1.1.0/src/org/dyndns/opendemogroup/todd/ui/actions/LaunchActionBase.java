package org.dyndns.opendemogroup.todd.ui.actions;

import org.dyndns.opendemogroup.todd.SimpleStructuredSelection;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.junit.launcher.JUnitLaunchShortcut;
import org.eclipse.jface.viewers.IStructuredSelection;

public abstract class LaunchActionBase extends ActionBase {

	@SuppressWarnings("restriction")
	private JUnitLaunchShortcut _Launcher;

	@SuppressWarnings("restriction")
	public LaunchActionBase() {
		_Launcher = new JUnitLaunchShortcut ( );
	}

	@SuppressWarnings("restriction")
	protected void launch ( IJavaElement targetElement, String mode ) {
		IStructuredSelection ss = new SimpleStructuredSelection ( targetElement );
		_Launcher.launch ( ss, mode);
	}
}
