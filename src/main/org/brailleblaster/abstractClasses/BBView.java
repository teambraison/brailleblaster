package org.brailleblaster.abstractClasses;

import org.brailleblaster.perspectives.braille.Manager;
import org.eclipse.swt.widgets.Group;

public interface BBView {
	public abstract void resetView(Group group);
	public abstract void initializeListeners(final Manager dm);
	public abstract void removeListeners();
}
