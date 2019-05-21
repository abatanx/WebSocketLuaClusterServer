/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.server;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class WebSock extends WebSocketServlet
{
	private static final long serialVersionUID = 1L;

	@Override
	public void configure(WebSocketServletFactory factory)
	{
		factory.register(WebSockMain.class);
	}
}