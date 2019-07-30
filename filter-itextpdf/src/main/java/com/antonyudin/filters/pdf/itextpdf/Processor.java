
/*
 * vim: set nowrap:
 *
 */

package com.antonyudin.filters.pdf.itextpdf;


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

		final com.itextpdf.html2pdf.ConverterProperties converterProperties =
			new com.itextpdf.html2pdf.ConverterProperties()
		;

		converterProperties.setCharset(entry.getEncoding());

		final com.itextpdf.styledxmlparser.css.media.MediaDeviceDescription mediaDeviceDescription =
			new com.itextpdf.styledxmlparser.css.media.MediaDeviceDescription(
				com.itextpdf.styledxmlparser.css.media.MediaType.PRINT
			)
		;

		converterProperties.setMediaDeviceDescription(mediaDeviceDescription);

		final com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(output);

		try (final com.itextpdf.kernel.pdf.PdfDocument pdfDocument = new com.itextpdf.kernel.pdf.PdfDocument(writer)) {

			if (entry.getPageSize() == Entry.PageSize.LETTER)
				pdfDocument.setDefaultPageSize(com.itextpdf.kernel.geom.PageSize.LETTER);

			if (entry.getOrientation() == Entry.Orientation.LANDSCAPE)
				pdfDocument.setDefaultPageSize(pdfDocument.getDefaultPageSize().rotate());

			com.itextpdf.html2pdf.HtmlConverter.convertToPdf(
				new java.io.ByteArrayInputStream(input),
				pdfDocument,
				converterProperties
			);
		}
	}

}

