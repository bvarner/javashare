package org.beShare.gui;

import org.beShare.DropMenuItemFactory;

class StringItemFactory implements DropMenuItemFactory<String> {
	@Override
	public String toString(String obj) {
		return obj;
	}

	@Override
	public String fromString(String obj) {
		return obj;
	}
}