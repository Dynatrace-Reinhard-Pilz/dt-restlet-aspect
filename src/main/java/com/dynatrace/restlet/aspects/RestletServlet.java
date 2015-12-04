package com.dynatrace.restlet.aspects;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Request;
import org.restlet.Response;

/**
 * An artificial Servlet which ensures that an incoming request is being
 * mimicked by a call to a Servlet.
 * 
 * @author reinhard.pilz@dynatrace.com
 *
 */
public class RestletServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private static final String ERRMSG_SERVICE =
			"Unable to simulate Servlet invocation successfully";
	
	private static final Logger LOGGER =
			Logger.getLogger(RestletServlet.class.getName());
	
	protected final Request request;
	protected final Response response;
	protected final Runnable proceedRunnable;
	
	public RestletServlet(
			Request request,
			Response response,
			Runnable proceedRunnable
	) {

		this.request = request;
		this.response = response;
		this.proceedRunnable = proceedRunnable;
	}

	/**
	 * @return always {@link VertxServletConfig#INSTANCE} because this Servlet
	 * 		has not really been configured in any ways
	 */
	@Override
	public ServletConfig getServletConfig() {
		return RestletServletConfig.INSTANCE;
	}

	/**
	 * @return always the simple class name of {@link VertxServlet}
	 */
	@Override
	public final String getServletName() {
		return RestletServlet.class.getSimpleName();
	}
	
	/**
	 * Any execution of {@link ServerConnection.handleRequest} is being
	 * wrapped by the calling this method, which in turn then invokes
	 * this artificial Servlet's {@code service} method.<br />
	 * <br />
	 * This ensures that the Dynatrace Servlet Sensor can pick up the
	 * request as PurePath
	 * 
	 * @param request the request object
	 * @param runnable a {@link Runnable} which is able to invoke the
	 * 		original business logic to be executed during this request
	 * 		cycle.
	 */
	public final void execute() {
		try {
			service(
				new RestletServletRequest(request),
				RestletServletResponse.INSTANCE
			);
		} catch (Throwable throwable) {
			LOGGER.log(Level.WARNING, ERRMSG_SERVICE, throwable);
		}
	}
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) {
		proceedRunnable.run();
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse res) {
		proceedRunnable.run();
	}
	
	@Override
	public void doPut(HttpServletRequest req, HttpServletResponse res) {
		proceedRunnable.run();
	}
	
	@Override
	public void doDelete(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException
	{
		proceedRunnable.run();
	}
	
	@Override
	public void doHead(HttpServletRequest req, HttpServletResponse res) {
		proceedRunnable.run();
	}
	
	@Override
	public final void doTrace(HttpServletRequest req, HttpServletResponse res) {
		proceedRunnable.run();
	}
	
	@Override
	public void doOptions(HttpServletRequest req, HttpServletResponse res) {
		proceedRunnable.run();
	}

}

