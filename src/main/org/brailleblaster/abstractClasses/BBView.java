package org.brailleblaster.abstractClasses;

import org.eclipse.swt.custom.SashForm;

public interface BBView {
	public abstract void resetView(SashForm sash);
	public abstract void initializeListeners();
	public abstract void removeListeners();
}
