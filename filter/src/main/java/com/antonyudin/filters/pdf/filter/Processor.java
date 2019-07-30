
/*
 * vim: set nowrap:
 *
 */

package com.antonyudin.filters.pdf.filter;


public interface Processor {


	public void process(
		final byte[] input,
		final java.io.OutputStream output,
		final Entry entry,
		final String url
	) throws java.lang.Exception;

}

