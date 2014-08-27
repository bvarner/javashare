package org.beShare.gui;

import javax.swing.JTable;
import javax.swing.table.TableModel;

public class QueryTable extends JTable {

	QueryTable(TableModel dm) {
		super(dm);
		setFocusable(false);
	}
}
