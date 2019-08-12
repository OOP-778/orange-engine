package org.brian.core.mi.designer;

public class Row {

    private RowType type;

    /*
    if configType == Number row should be defined!
    */

    private int row = 0;

    public Row(RowType rowType) {
        this.type = rowType;
    }

    public Row(RowType rowType, Integer row) {
        this.type = rowType;
        this.row = row;
    }

    public int getRow() {
        return row;
    }

    public RowType getType() {
        return type;
    }
}
