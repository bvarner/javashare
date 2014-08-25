package org.beShare.gui;

public class StringDropMenuModel extends AbstractDropMenuModel<String> {
	public StringDropMenuModel() {
		super();
	}

	public StringDropMenuModel(int size) {
		super(size);
	}

	@Override
	public String elementToString(String obj) {
		return obj;
	}

	@Override
	public String elementFromString(String obj) {
		return obj;
	}
}
