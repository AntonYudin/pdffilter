
/*
 * vim: set nowrap:
 *
 */

package com.antonyudin.filters.pdf.filter;


import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@javax.servlet.annotation.WebFilter("/*")
public class Filter extends javax.servlet.http.HttpFilter {

	private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
		Filter.class.getName()
	);


	public static class BufferedResponse extends javax.servlet.http.HttpServletResponseWrapper {

		public BufferedResponse(final javax.servlet.http.HttpServletResponse response) throws java.lang.Exception {
			super(response);
		}


		private final java.io.ByteArrayOutputStream outputStream =
			new java.io.ByteArrayOutputStream()
		;

		private final java.security.DigestOutputStream digestOutputStream =
			new java.security.DigestOutputStream(
				outputStream,
				java.security.MessageDigest.getInstance("SHA-256")
			)
		;

		private final javax.servlet.ServletOutputStream servletOutputStream = new javax.servlet.ServletOutputStream() {

			private javax.servlet.WriteListener writeListener = null;

			@Override
			public void write(final int value) throws java.io.IOException {
			//	logger.info("write: [" + value + "]");
				digestOutputStream.write(value);
			}

			@Override
			public void setWriteListener(final javax.servlet.WriteListener writeListener) {
				this.writeListener = writeListener;
			}

			@Override
			public boolean isReady() {
				return true;
			}

		};

		private final java.io.PrintWriter printWriter = new java.io.PrintWriter(servletOutputStream);



		@Override
		public javax.servlet.ServletOutputStream getOutputStream() {
			logger.info("getOutputStream()");
			return servletOutputStream;
		}


		@Override
		public java.io.PrintWriter getWriter() {
			logger.info("getWriter()");
			return printWriter;
		}

		public byte[] getContent() {
			return outputStream.toByteArray();
		}

		@Override
		public void reset() {
			logger.info("reset()");
			super.reset();
			outputStream.reset();
		}

		@Override
		public void resetBuffer() {
			logger.info("resetBuffer()");

			super.resetBuffer();

			try {
				digestOutputStream.flush();
			} catch (java.lang.Exception exception) {
				throw new IllegalArgumentException(exception);
			}

			outputStream.reset();
			digestOutputStream.getMessageDigest().reset();
		}

		public String getDigest() throws java.io.IOException {

			printWriter.flush();

			digestOutputStream.flush();

			return java.util.Base64.getEncoder().encodeToString(
				digestOutputStream.getMessageDigest().digest()
			);
		}

	}


	private boolean printContent = false; 


	@Override
	public void init(final javax.servlet.FilterConfig filterConfig) throws javax.servlet.ServletException {

		logger.info("init(" + filterConfig + ")");

		try {
			{
				final String name = getClass().getPackage().getName() + "." + "processor";

				final String value = filterConfig.getServletContext().getInitParameter(name);

				if ((value != null) && value.equalsIgnoreCase("true"))
					defaultProcessor = (Processor) Class.forName(value).getDeclaredConstructor().newInstance();
				else
					defaultProcessor = (Processor) Class.forName("com.antonyudin.filters.pdf.openhtmltopdf.Processor").getDeclaredConstructor().newInstance();

				logger.info("selected processor: [" + defaultProcessor + "]");
			}


			{
				final String name = getClass().getPackage().getName() + "." + "printContent";

				final String value = filterConfig.getServletContext().getInitParameter(name);

				if ((value != null) && value.equalsIgnoreCase("true"))
					printContent = true;
			}

			for (int i = 0;; i++) {

				final String name = getClass().getPackage().getName() + "." + i;

				logger.info("checking [" + name + "]");

				final String value = filterConfig.getServletContext().getInitParameter(name);

				if (value == null)
					break;

				entries.add(new Entry(value.split(";")));

			}
		} catch (java.lang.Exception exception) {
			throw new javax.servlet.ServletException(exception);
		}
	}

	private final List<Entry> entries = new ArrayList<>();


	public static class CacheEntry implements java.io.Serializable {

		public CacheEntry(final String digest, final byte[] content) {
			this.digest = digest;
			this.content = content;
		}


		private final String digest;

		public String getDigest() {
			return digest;
		}


		private final byte[] content;

		public byte[] getContent() {
			return content;
		}


		@Override
		public String toString() {
			return getDigest();
		}
	}


	private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();


	private Processor defaultProcessor = null;


	@Override
	protected void doFilter(
		final javax.servlet.http.HttpServletRequest request,
		final javax.servlet.http.HttpServletResponse response,
		final javax.servlet.FilterChain chain
	) throws java.io.IOException, javax.servlet.ServletException {

		logger.info("doFilter(" + request + ")");

		final String url = (
			request.getServletPath() +
			(
				request.getPathInfo() != null?
				request.getPathInfo():
				""
			) +
			(
				request.getQueryString() != null?
				"?" + request.getQueryString():
				""
			)
		);

		Entry foundEntry = null;

		for (Entry entry: entries) {
			if (url.startsWith(entry.getPrefix())) {
				foundEntry = entry;
				break;
			}
		}

		if (foundEntry == null) {
			logger.info("skipping: [" + url + "]");
			chain.doFilter(request, response);
			return;
		}


		try {

			logger.info("processing: [" + url + "]");

			final BufferedResponse bufferedResponse = new BufferedResponse(response);

			chain.doFilter(request, bufferedResponse);

			final String digest = bufferedResponse.getDigest();

			if (printContent) {
				logger.info(
					"buffered response content: [" +
					(new String(bufferedResponse.getContent(), "UTF-8")) +
					"]"
				);
			}

			logger.info("buffered response digest: [" + digest + "]");

			CacheEntry cacheEntry = cache.get(url);

			logger.info("cache entry: [" + cacheEntry + "]");

			if (
				(!foundEntry.isCache()) ||
				(cacheEntry == null) ||
				(!cacheEntry.getDigest().equals(digest))
			) {

				final java.io.ByteArrayOutputStream result = new java.io.ByteArrayOutputStream();

				logger.info("converting to pdf ...");

				defaultProcessor.process(
					bufferedResponse.getContent(),
					result,
					foundEntry,
					request.getRequestURL().toString()
				);

				logger.info("converting to pdf ... done, [" + result.size() + "] bytes.");

				cacheEntry = new CacheEntry(digest, result.toByteArray());

				if (foundEntry.isCache())
					cache.put(url, cacheEntry);
			}

			response.reset();

			response.setContentType("application/pdf");
			response.setCharacterEncoding(null);

			response.getOutputStream().write(cacheEntry.getContent());

			response.flushBuffer();

		} catch (java.lang.Exception exception) {
			logger.log(java.util.logging.Level.SEVERE, "exception: " + exception, exception);
			throw new javax.servlet.ServletException(exception);
		}
	}

}

