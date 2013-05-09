package org.brailleblaster.wordprocessor;

public enum BBEvent {
	INCREMENT,
	DECREMENT,
	TEXT_DELETION,
	UPDATE,
	REMOVE_NODE,
	SET_CURRENT,
	GET_CURRENT,
	ADJUST_ALIGNMENT,
	ADJUST_INDENT,
	ADJUST_RANGE;
}
