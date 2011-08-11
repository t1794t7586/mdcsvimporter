package com.moneydance.modules.features.mdcsvimporter;

import java.util.ArrayList;

/**
 *
 * @author Stan Towianski
 */


public class CustomReaderData {

//    ArrayList<javax.swing.JComboBox> dataTypesList = new ArrayList<javax.swing.JComboBox>( 10 );
//    ArrayList<javax.swing.JComboBox> emptyFlagsList = new ArrayList<javax.swing.JComboBox>( 10 );
    ArrayList<String> dataTypesList = new ArrayList<String>( 10 );
    ArrayList<String> emptyFlagsList = new ArrayList<String>( 10 );
    int fieldSeparatorChar = ',';
    int headerLines = 0;
    String readerName = "";

    public ArrayList<String> getDataTypesList() {
        return dataTypesList;
    }

    public void setDataTypesList(ArrayList<String> dataTypesList) {
        this.dataTypesList = dataTypesList;
    }

    public ArrayList<String> getEmptyFlagsList() {
        return emptyFlagsList;
    }

    public void setEmptyFlagsList(ArrayList<String> emptyFlagsList) {
        this.emptyFlagsList = emptyFlagsList;
    }

    public int getFieldSeparatorChar() {
        return fieldSeparatorChar;
    }

    public void setFieldSeparatorChar(int fieldSeparatorChar) {
        this.fieldSeparatorChar = fieldSeparatorChar;
    }

    public int getHeaderLines() {
        return headerLines;
    }

    public void setHeaderLines(int headerLines) {
        this.headerLines = headerLines;
    }

    public String getReaderName() {
        return readerName;
    }

    public void setReaderName(String readerName) {
        this.readerName = readerName;
    }
    
}
