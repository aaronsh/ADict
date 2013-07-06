package com.benemind.adict.core.kingsoft;

class IcibaConvertParam {
	protected enum ExcelColumnIndex {
		Tag, BeginHtmlTag, NodeName, OnClick, BeginText, ShowTag, FollowText, EndText, EndHtmlTag, Comment
	}

	private String[] Cells;

	protected IcibaConvertParam() {
		ExcelColumnIndex items[] = ExcelColumnIndex.values();
		Cells = new String[items.length];
	}

	protected String get(int columnIndex) {
		if (columnIndex < Cells.length) {
			return Cells[columnIndex];
		}
		return null;
	}

	protected String get(ExcelColumnIndex columnName) {
		int index = columnName.ordinal();
		return Cells[index];
	}

	protected void set(int index, String val) {
		if (index < Cells.length) {
			Cells[index] = val;
		}
	}

	protected void set(ExcelColumnIndex name, String val) {
		int index = name.ordinal();
		Cells[index] = val;
	}

	protected int getIndex(ExcelColumnIndex name) {
		return name.ordinal();
	}

}
