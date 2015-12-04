package com.dynatrace.restlet.aspects;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;

public aspect RestletAspect {
	
	private static final Logger LOGGER =
			Logger.getLogger(RestletAspect.class.getName());

	/**
	 * Around executions of {@link Server.handle} we are
	 * creating an artificial call to a Servlet, which allows the
	 * Dynatrace Servlet Sensor to pick up the request as a Pure Path
	 *   
	 * @param req the request object
	 * @param resp the response object
	 */
	void around(
		final Request req,
		final Response resp
	):
		execution(
			void org.restlet.Server.handle(Request, Response)
		)
		&&
		args(req, resp)
	{
		final AtomicBoolean hasBeenExecuted = new AtomicBoolean(false);
		final Runnable proceedRunnable = new Runnable() {
			@Override
			public void run() {
				hasBeenExecuted.set(true);
				proceed(req, resp);
			}
		};
		try {
			new RestletServlet(req, resp, proceedRunnable).execute();
		} catch (Throwable t) {
			LOGGER.log(Level.WARNING, "Servlet Invocation failed",	t);
		} finally {
			if (!hasBeenExecuted.get()) {
				proceed(req, resp);
			}
		}
	}	
}
