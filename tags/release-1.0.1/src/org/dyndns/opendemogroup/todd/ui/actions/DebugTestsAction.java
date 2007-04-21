package org.dyndns.opendemogroup.todd.ui.actions;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

public class DebugTestsAction extends LaunchActionBase  {

	/**
	 * @see org.dyndns.opendemogroup.todd.ui.actions.ActionBase#run(org.eclipse.jdt.core.IMethod)
	 */
	@Override
	protected void run(IMethod targetMethod) {
		launch ( targetMethod, "debug" );
	}

	/**
	 * @see org.dyndns.opendemogroup.todd.ui.actions.ActionBase#run(org.eclipse.jdt.core.IType)
	 */
	@Override
	protected void run(IType targetType) {
		launch ( targetType, "debug" );
	}

}
