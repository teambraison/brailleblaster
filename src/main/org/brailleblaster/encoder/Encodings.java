package org.brailleblaster.encoder;

public enum Encodings {
	ISO_8859_1("ISO-8859-1"), UTF_8("UTF-8"), WINDOWS_1252("WINDOWS-1252"), US_ASCII(
			"US-ASCII");

	private final String encoding;

	Encodings(String encoding) {
		this.encoding = encoding;
	}

	public String encoding() {
		return encoding;
	}
}
