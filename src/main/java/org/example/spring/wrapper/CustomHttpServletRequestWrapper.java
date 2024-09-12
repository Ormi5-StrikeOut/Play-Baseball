package org.example.spring.wrapper;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {
	private final Map<String, String> customHeaders;

	/**
	 * Constructs a request object wrapping the given request.
	 *
	 * @param request The request to wrap
	 * @throws IllegalArgumentException if the request is null
	 */
	public CustomHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
		this.customHeaders = new HashMap<>();
	}

	public void putHeader(String name, String value) {
		this.customHeaders.put(name, value);
	}

	@Override
	public String getHeader(String name) {
		String headerValue = customHeaders.get(name);
		if (headerValue != null) {
			return headerValue;
		}
		return super.getHeader(name);
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		Set<String> set = new HashSet<>(customHeaders.keySet());
		Enumeration<String> e = ((HttpServletRequest)getRequest()).getHeaderNames();
		while (e.hasMoreElements()) {
			String n = e.nextElement();
			set.add(n);
		}
		return Collections.enumeration(set);
	}
}
