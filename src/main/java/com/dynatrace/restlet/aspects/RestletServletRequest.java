package com.dynatrace.restlet.aspects;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;


/**
 * <p>
 * In order to provide the required information about the HTTP request
 * to the dynaTrace Servlet Sensor the internal
 * {@link DefaultHttpServerRequest} object is being wrapped and instead
 * offered as a {@link HttpServletRequest}.
 * </p>
 * <p>
 * Not all methods offered by {@link HttpServletRequest} are required
 * to deliver proper values since they are not being queried by the
 * dynaTrace Servlet Sensor anyways.
 * </p>
 * 
 * @author reinhard.pilz@dynatrace.com
 *
 */
public final class RestletServletRequest implements HttpServletRequest {
	
	/**
	 * A Server Name to offer during Servlet Invocation
	 */
	private static final String SERVER_NAME = "restlet";
	
	/**
	 * The internal request object of vertx
	 */
	private final Object request;
	
	@SuppressWarnings("unused")
	private static String stackTraceToString(Throwable t) {
		if (t == null) {
			return "";
		}
		StackTraceElement[] stackTrace = t.getStackTrace();
		if (stackTrace == null) {
			return "<no stacktrace available>";
		}
		try (
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
		) {
			for (StackTraceElement element : stackTrace) {
				pw.println(element.toString());
			}
			return sw.getBuffer().toString();
		} catch (IOException e) {
			e.printStackTrace(System.err);
			return e.getMessage();
		}
	}
	
	private static void error(Throwable t) {
		if (t == null) {
			return;
		}
		t.printStackTrace(System.err);
	}
	
	private static java.lang.reflect.Method getMethod(Class<?> clazz, String name, Class<?>[] paramTypes) {
		if (clazz == null) {
			return null;
		}
		if (clazz.getName().equals(Object.class.getName())) {
			return null;
		}
		if (name == null) {
			return null;
		}
		try {
			return clazz.getDeclaredMethod(name, paramTypes);
		} catch (NoSuchMethodException e) {
			return getMethod(clazz.getSuperclass(), name, paramTypes);
		} catch (Throwable t) {
			error(t);
			return null;
		}
	}
	
	private static <T> T get(Object o, String methodName, Boolean param) {
		return get(o, methodName, new Class<?>[] { Boolean.class }, new Object[] { param });
	}
	
	private static <T> T get(Object o, String methodName, String param) {
		return get(o, methodName, new Class<?>[] { String.class }, new Object[] { param });
	}
	
	private static <T> T get(Object o, String methodName) {
		return get(o, methodName, new Class<?>[] {}, new Object[] {});
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T get(Object o, String methodName, Class<?>[] paramTypes, Object[] params) {
		if (o == null) {
			return null;
		}
		Class<? extends Object> clazz = o.getClass();
		java.lang.reflect.Method method = getMethod(clazz, methodName, paramTypes);
		if (method == null) {
			return null;
		}
		method.setAccessible(true);
		try {
			return (T) method.invoke(o, params);
		} catch (Throwable t) {
			error(t);
			return null;
		}
	}
	
	/**
	 * c'tor
	 * 
	 * @param request the internal request object of vertx
	 */
	public RestletServletRequest(Object request) {
		this.request = request;
	}

	/**
	 * @return the request method of the HTTP request (GET, POST, ...)
	 */
	@Override
	public String getMethod() {
		if (request == null) {
			return "GET";
		}
		Object method = get(request, "getMethod");
		if (method == null) {
			return "GET";
		}
		return method.toString();
	}
	
	/**
	 * @return always the same server name since there does not exist such a
	 * 		feature of naming the server in vertx
	 */
	@Override
	public String getServerName() {
		return SERVER_NAME;
	}
	
	/**
	 * @return the client IP address of the HTTP request
	 */
	@Override
	public String getRemoteAddr() {
		try {
			if (request == null) {
				return null;
			}
			Object clientInfo = get(request, "getClientInfo");
			if (clientInfo == null) {
				return null;
			}
			return get(clientInfo, "getAddress");
		} catch (Throwable t) {
			error(t);
			return null;
		}
	}

	/**
	 * @return the client host name if applicable of the HTTP request
	 */
	@Override
	public String getRemoteHost() {
		return getRemoteAddr();
	}
	
	/**
	 * @return the request URI of the HTTP request
	 */
	@Override
	public String getRequestURI() {
		try {
			if (request == null) {
				return null;
			}
			Object ref = get(request, "getResourceRef");
			if (ref == null) {
				return null;
			}
			return get(ref, "getPath");
		} catch (Throwable t) {
			error(t);
			return null;
		}
	}
	
	/**
	 * @return the query string of the HTTP request
	 */
	@Override
	public String getQueryString() {
		try {
			if (request == null) {
				return null;
			}
			Object ref = get(request, "getResourceRef");
			if (ref == null) {
				return null;
			}
			return get(ref, "getQuery");
		} catch (Throwable t) {
			error(t);
			return null;
		}
	}

	/**
	 * @return the protocol version of the HTTP request, either {@code HTTP/1.1}
	 * 		or {@code HTTP/1.0}
	 */
	@Override
	public String getProtocol() {
		try {
			if (request == null) {
				return "HTTP/1.1";
			}
			Object protocol = get(request, "getProtocol");
			if (protocol == null) {
				return "HTTP/1.1";
			}
			Object sVersion = get(protocol, "getVersion");
			if (sVersion == null) {
				return "HTTP/1.1";
			}
			return "HTTP/" + sVersion;
		} catch (Throwable t) {
			error(t);
			return "HTTP/1.1";
		}
	}

	/**
	 * @return the request header with the given name or {@code null} if no
	 * 		header with the given name has been sent during the HTTP request
	 */
	@Override
	public String getHeader(String name) {
		String result = getHeader0(name);
		if (result != null) {
			return result;
		}
		if (name == null) {
			return null;
		}
		if (name.toLowerCase().contains("dynatrace")) {
			Enumeration<String> headerNames = getHeaderNames0();
			if (headerNames == null) {
				return null;
			}
			while (headerNames.hasMoreElements()) {
				String headerName = headerNames.nextElement();
				if (name.toLowerCase().equals(headerName.toLowerCase())) {
					return getHeader0(headerName);
				}
			}
		}
		return null;
	}
	
	public String getHeader0(String name) {
		try {
			Object headers = get(request, "getHeaders");
        	if (headers == null) {
        		return null;
        	}
        	Object isEmpty = get(headers, "isEmpty");
        	if (Boolean.TRUE.equals(isEmpty)) {
        		return null;
        	}
        	return get(headers, "getFirstValue", name);
		} catch (Throwable t) {
			error(t);
			return null;
		}
	}
	
	@Override
	public Enumeration<String> getHeaders(String name) {
		Enumeration<String> result = getHeaders0(name);
		if ((result != null) && result.hasMoreElements()) {
			return result;
		}
		if (name == null) {
			return Collections.emptyEnumeration();
		}
		if (name.toLowerCase().contains("dynatrace")) {
			Enumeration<String> headerNames = getHeaderNames0();
			if (headerNames == null) {
				return null;
			}
			while (headerNames.hasMoreElements()) {
				String headerName = headerNames.nextElement();
				if (name.toLowerCase().equals(headerName.toLowerCase())) {
					return getHeaders0(headerName);
				}
			}
		}
		return Collections.emptyEnumeration();
	}
	
	/**
	 * @return all the values of the headers with the given name or an empty
	 * 		{@link Enumeration} if no such head has been sent during the
	 * 		HTTP request
	 */
	public Enumeration<String> getHeaders0(String name) {
		try {
			Object headers = get(request, "getHeaders");
        	if (headers == null) {
        		return Collections.emptyEnumeration();
        	}
        	Object isEmpty = get(headers, "isEmpty");
        	if (Boolean.TRUE.equals(isEmpty)) {
        		return Collections.emptyEnumeration();
        	}
        	String[] values = get(headers, "getValuesArray", name);
        	if ((values == null) || (values.length == 0)) {
        		return Collections.emptyEnumeration();
        	}
        	Vector<String> v = new Vector<String>();
        	v.addAll(Arrays.asList(values));
        	return v.elements();
        } catch (Throwable t) {
			error(t);
			return Collections.emptyEnumeration();
		}
	}
	
	
	/**
	 * @return always {@code null} because the internal representation of the
	 * 		HTTP request of vertx does not offer methods to query for cookies.
	 * 		It is possible to parse the request headers manually and produce
	 * 		the {@link Cookie} values here, but it is currently not implemented 
	 */
	@Override
	public Cookie[] getCookies() {
		return null;
	}
	
	/**
	 * @return the URL of the HTTP request
	 */
	@Override
	public StringBuffer getRequestURL() {
		try {
			String uri = getRequestURI();
			if (uri == null) {
				return new StringBuffer();
			}
			return new StringBuffer(uri);
			
		} catch (Throwable t) {
			error(t);
			return new StringBuffer();
		}
	}

	/**
	 * @return the value of the request parameter passed within the query string
	 * 		with the given name or {@code null} if no such parameter has been
	 * 		passed with this HTTP request. This method will not take POST
	 * 		parameters into considerations because their values are not known
	 * 		until the request body has been parsed, which is not being ensured
	 * 		by vertx once the internal representation of the HTTP request is
	 * 		being created and handed over to the request handlers.
	 */
	@Override
	public String getParameter(String name) {
		try {
			if (request == null) {
				return null;
			}
			Object ref = get(request, "getResourceRef");
			if (ref == null) {
				return null;
			}
			Object form = get(ref, "getQueryAsForm", true);
			if (form == null) {
				return null;
			}
			Object param = get(form, "getFirst", name);
			if (param == null) {
				return null;
			}
			return get(param, "getValue");
		} catch (Throwable t) {
			error(t);
			return null;
		}
	}

	/**
	 * @return the names of all request parameters passed within the query
	 * 		string of the HTTP request
	 */
	@Override
	public Enumeration<String> getParameterNames() {
		try {
			if (request == null) {
				return Collections.emptyEnumeration();
			}
			Object ref = get(request, "getResourceRef");
			if (ref == null) {
				return Collections.emptyEnumeration();
			}
			Object form = get(ref, "getQueryAsForm", true);
			if (form == null) {
				return Collections.emptyEnumeration();
			}
			Map<String, String> names = get(form, "getValuesMap");
			if (names == null) {
				return Collections.emptyEnumeration();
			}
			final Iterator<Entry<String, String>> it = names.entrySet().iterator();
			return new Enumeration<String>() {

				@Override
				public boolean hasMoreElements() {
					return it.hasNext();
				}

				@Override
				public String nextElement() {
					return it.next().getKey();
				}
			};
		} catch (Throwable t) {
			error(t);
			return Collections.emptyEnumeration();
		}
	}

	/**
	 * @return all values of the request parameters matching the given name
	 * 		passed within the query string of the HTTP request or {@code null}
	 * 		if no such parameter exists
	 */
	@Override
	public String[] getParameterValues(String name) {
		try {
			if (request == null) {
				return new String[0];
			}
			Object ref = get(request, "getResourceRef");
			if (ref == null) {
				return new String[0];
			}
			Object form = get(ref, "getQueryAsForm", true);
			if (form == null) {
				return new String[0];
			}
			String[] values = get(form, "getValuesArray", name);
			if (values == null) {
				return new String[0];
			}
			return values;
		} catch (Throwable t) {
			error(t);
			return new String[0];
		}
	}

	/**
	 * @return the names of all headers passed with the HTTP request
	 */
	@Override
	public Enumeration<String> getHeaderNames() {
		try {
			Object headers = get(request, "getHeaders");
        	if (headers == null) {
        		return Collections.emptyEnumeration();
        	}
        	Object isEmpty = get(headers, "isEmpty");
        	if (Boolean.TRUE.equals(isEmpty)) {
        		return Collections.emptyEnumeration();
        	}
        	Set<String> names = get(headers, "getNames");
        	if ((names == null) || names.isEmpty()) {
        		return Collections.emptyEnumeration();
        	}
        	Vector<String> v = new Vector<String>();
        	for (String name : names) {
				if (name == null) {
					continue;
				}
				String n = name;
				if (n.toLowerCase().contains("dynatrace")) {
					n = n.replace("dynatrace", "dynaTrace");
				}
				v.add(n);
			}
        	return v.elements();
        } catch (Throwable t) {
			error(t);
			return Collections.emptyEnumeration();
		}
	}
	
	public Enumeration<String> getHeaderNames0() {
		try {
			Object headers = get(request, "getHeaders");
        	if (headers == null) {
        		return Collections.emptyEnumeration();
        	}
        	Object isEmpty = get(headers, "isEmpty");
        	if (Boolean.TRUE.equals(isEmpty)) {
        		return Collections.emptyEnumeration();
        	}
        	Set<String> names = get(headers, "getNames");
        	if ((names == null) || names.isEmpty()) {
        		return Collections.emptyEnumeration();
        	}
        	Vector<String> v = new Vector<String>();
			v.addAll(names);
        	return v.elements();
        } catch (Throwable t) {
			error(t);
			return Collections.emptyEnumeration();
		}
	}	
	
	/**
	 * @return always {@code null} because setting request attributes is not
	 * 		supported in vertx
	 */
	@Override
	public Object getAttribute(String name) {
		ConcurrentMap<String, Object> attributes = get(request, "getAttributes");
		if (attributes == null) {
			return null;
		}
		return attributes.get(name);
	}

	/**
	 * @return always an empty {@link Enumeration} because setting request
	 * 		attributes is not supported in vertx
	 */
	@Override
	public Enumeration<String> getAttributeNames() {
		ConcurrentMap<String, Object> attributes = get(request, "getAttributes");
		if (attributes == null) {
			return Collections.emptyEnumeration();
		}
		Set<String> names = attributes.keySet();
		Vector<String> v = new Vector<String>();
		v.addAll(names);
		return v.elements();
	}

	/**
	 * @return always {@code null}. There is a chance to implement this method
	 * 		by parsing the HTTP header {@code Content-Encoding} but it is not
	 * 		being queried for by the dynaTrace Servlet Sensor, so there would
	 * 		be no consumer for this value.
	 */
	@Override
	public String getCharacterEncoding() {
		return null;
	}

	/**
	 * ignored
	 */
	@Override
	public void setCharacterEncoding(String env)
			throws UnsupportedEncodingException {
	}

	/**
	 * @return always {@code 0}, not being queried
	 */
	@Override
	public int getContentLength() {
		return 0;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public String getContentType() {
		return null;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public ServletInputStream getInputStream() throws IOException {
		return null;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public Map<String, String[]> getParameterMap() {
		return null;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public String getScheme() {
		return null;
	}

	/**
	 * @return always {@code 0}, not being queried
	 */
	@Override
	public int getServerPort() {
		return 0;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public BufferedReader getReader() throws IOException {
		return null;
	}

	/**
	 * ignored because vertx does not support request attributes
	 */
	@Override
	public void setAttribute(String name, Object o) {
	}

	/**
	 * ignored because vertx does not support request attributes
	 */
	@Override
	public void removeAttribute(String name) {
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public Locale getLocale() {
		return null;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public Enumeration<Locale> getLocales() {
		return null;
	}

	/**
	 * @return always {@code false}, not being queried
	 */
	@Override
	public boolean isSecure() {
		return false;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return null;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public String getRealPath(String path) {
		return null;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public int getRemotePort() {
		return 0;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public String getLocalName() {
		return null;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public String getLocalAddr() {
		return null;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public int getLocalPort() {
		return 0;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public ServletContext getServletContext() {
		return null;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		return null;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public AsyncContext startAsync(ServletRequest servletRequest,
			ServletResponse servletResponse) throws IllegalStateException {
		return null;
	}

	/**
	 * @return always {@code false}, not being queried
	 */
	@Override
	public boolean isAsyncStarted() {
		return false;
	}

	/**
	 * @return always {@code false}, not being queried
	 */
	@Override
	public boolean isAsyncSupported() {
		return false;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public AsyncContext getAsyncContext() {
		return null;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public DispatcherType getDispatcherType() {
		return null;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public String getAuthType() {
		return null;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public long getDateHeader(String name) {
		return 0;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public int getIntHeader(String name) {
		return 0;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public String getPathInfo() {
		return null;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public String getPathTranslated() {
		return null;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public String getContextPath() {
		return null;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public String getRemoteUser() {
		return null;
	}

	/**
	 * @return always {@code false}, not being queried
	 */
	@Override
	public boolean isUserInRole(String role) {
		return false;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public Principal getUserPrincipal() {
		return null;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public String getRequestedSessionId() {
		return null;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public String getServletPath() {
		return null;
	}

	/**
	 * @return always {@code null}, since sessions are not supported by vertx
	 */
	@Override
	public HttpSession getSession(boolean create) {
		return null;
	}

	/**
	 * @return always {@code null}, since sessions are not supported by vertx
	 */
	@Override
	public HttpSession getSession() {
		return null;
	}

	/**
	 * @return always {@code false}, not being queried
	 */
	@Override
	public boolean isRequestedSessionIdValid() {
		return false;
	}

	/**
	 * @return always {@code false}, not being queried
	 */
	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return false;
	}

	/**
	 * @return always {@code false}, not being queried
	 */
	@Override
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	/**
	 * @return always {@code false}, not being queried
	 */
	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	/**
	 * @return always {@code false}, not being queried
	 */
	@Override
	public boolean authenticate(HttpServletResponse response)
			throws IOException, ServletException {
		return false;
	}

	/**
	 * ignored
	 */
	@Override
	public void login(String username, String password) throws ServletException {
	}

	/**
	 * ignored
	 */
	@Override
	public void logout() throws ServletException {
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		return null;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public Part getPart(String name) throws IOException, ServletException {
		return null;
	}

	/**
	 * @return always {@code 0}, not being queried
	 */
	@Override
	public long getContentLengthLong() {
		return 0;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public String changeSessionId() {
		return null;
	}

	/**
	 * @return always {@code null}, not being queried
	 */
	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass)
			throws IOException, ServletException {
		return null;
	}

}
