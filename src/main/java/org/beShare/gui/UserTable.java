package org.beShare.gui;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.util.Vector;

public class UserTable extends JTable {
	UserTable(){
		super();
	}
	
	UserTable(int numRows, int numColumns){
		super(numRows, numColumns);
	}
	
	UserTable(Object[][] rowData, Object[] columnNames){
		super(rowData, columnNames);
	}
	
	UserTable(TableModel dm){
		super(dm);
	}
	
	UserTable(TableModel dm, TableColumnModel cm){
		super(dm, cm);
	}
	
	UserTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm){
		super(dm, cm, sm);
	}
	
	UserTable(Vector rowData, Vector columnNames){
		super(rowData, columnNames);
	}
	
	public boolean isManagingFocus(){
		return false;
	}
}
