
/*
 * vim: set nowrap:
 *
 */

package com.antonyudin.filters.pdf.openhtmltopdf;


import com.antonyudin.filters.pdf.filter.Entry;



public class Processor implements com.antonyudin.filters.pdf.filter.Processor {

	private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
		Processor.class.getName()
	);


	@Override
	public void process(
		final byte[] input,
		final java.io.OutputStream output,
		final Entry entry,
		final String url
	) throws java.lang.Exception {

		final var builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();

		builder.useFastMode();

		logger.info("using base URL: [" + url + "]");

		builder.withHtmlContent(new String(input, "UTF-8"), url);

		builder.toStream(output);

		builder.run();
	}

}

