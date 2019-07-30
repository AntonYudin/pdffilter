
/*
 * vim: set nowrap:
 *
 */

package com.antonyudin.filters.pdf.filter;



public class Entry implements java.io.Serializable {

	private final String[] parameters;


	public enum PageSize {
		LETTER
	}

	public enum Orientation {
		PORTRAIT,
		LANDSCAPE
	}

	public Entry(final String[] parameters) {

		this.parameters = parameters;

		var pageSize = PageSize.LETTER;
		var orientation = Orientation.PORTRAIT;

		boolean cache = false;
		String encoding = "UTF-8";

		for (String parameter: parameters[0].split(",")) {

			if (parameter.equals("letter"))
				pageSize = PageSize.LETTER;
			else if (parameter.equals("landscape"))
				orientation = Orientation.LANDSCAPE;
			else if (parameter.equals("cache"))
				cache = true;
		}

		this.pageSize = pageSize;
		this.cache = cache;
		this.encoding = encoding;
		this.orientation = orientation;
	}

	public String getPrefix() {
		return parameters[1];
	}


	private final PageSize pageSize;

	public PageSize getPageSize() {
		return pageSize;
	}


	private final Orientation orientation;

	public Orientation getOrientation() {
		return orientation;
	}


	private final boolean cache;

	public boolean isCache() {
		return cache;
	}


	private final String encoding;

	public String getEncoding() {
		return encoding;
	}

}

