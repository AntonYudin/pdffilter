# pdffilter
XHTML To PDF Filter

This is a javax.servlet filter that converts xhtml pages with styles to a PDF document in real-time. The filter can be used to publish any xhtml resource (jsf or static) as a PDF document. The filter supports caching. The original document is MessageDigest-ed and a copy of the resulted PDF is kept in a ConcurrentHashMap. If the original document's digest changes, the PDF is regenerated. This functionality can be turned off.

The actual transformation of the XHTML into PDF is done using one of the following libraries:

* [ITextPDF/pdfHTML](https://itextpdf.com)
* [openhtmltopdf](https://github.com/danfickle/openhtmltopdf)

