package org.brailleblaster.unitTests;

import org.brailleblaster.unitTests.undoTests.ComplexUndoTests;
import org.brailleblaster.unitTests.undoTests.UndoEditTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ReadOnlyCursorTests.class, BoxlineTests.class, BoxlineSelectionTests.class, StyleChangeTests.class, EditingTests.class, PageSelectionTests.class,
	HideActionTests.class, UndoEditTests.class, ComplexUndoTests.class})

public class AllSelectionTests {  
	
}
