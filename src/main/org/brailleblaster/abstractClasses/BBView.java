package org.brailleblaster.abstractClasses;

import org.eclipse.swt.widgets.Group;

public interface BBView {
	public abstract void resetView(Group group);
	public abstract void initializeListeners();
	public abstract void removeListeners();
}
