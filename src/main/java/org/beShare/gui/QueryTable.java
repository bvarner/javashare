package org.beShare.gui;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.util.Vector;

public class QueryTable extends JTable {
	QueryTable(){
		super();
	}
	
	QueryTable(int numRows, int numColumns){
		super(numRows, numColumns);
	}
	
	QueryTable(Object[][] rowData, Object[] columnNames){
		super(rowData, columnNames);
	}
	
	QueryTable(TableModel dm){
		super(dm);
	}
	
	QueryTable(TableModel dm, TableColumnModel cm){
		super(dm, cm);
	}
	
	QueryTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm){
		super(dm, cm, sm);
	}
	
	QueryTable(Vector rowData, Vector columnNames){
		super(rowData, columnNames);
	}
	
	public boolean isManagingFocus(){
		return false;
	}
}
