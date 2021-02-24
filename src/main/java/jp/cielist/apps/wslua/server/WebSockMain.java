/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.server;

import jp.cielist.apps.wslua.common.Log;
import jp.cielist.apps.wslua.lua.JSONStringToProtocol;
import jp.cielist.apps.wslua.lua.JSONStringToValue;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebSocket
public class WebSockMain
{
	// Socket connection
	private Session session;

	// Lua
	private LuaEnv lua;

	public LuaEnv getLua()
	{
		return lua;
	}

	@OnWebSocketConnect
	synchronized public void onConnect(Session session)
	{
		Log.debug("Connected from %s", CSSessionSupport.getRemoteAddress(session));

//		session.getUpgradeRequest().getHeaders().forEach((k,v) ->
//		{
//			Log.debug("UpgradeRequest: %s %s", k, v);
//		});

//		session.getUpgradeResponse().getHeaders().forEach((k,v) ->
//		{
//			Log.debug("UpgradeResponse: %s %s", k, v);
//		});

		this.session = session;

		lua = new LuaEnv(session, false);

		CS.clientManager.add(this);
	}

	@OnWebSocketMessage
	synchronized public void onText(String message) {
		//Log.debug("Received from %s", session.getRemoteAddress().toString());
		Log.receiveLog(message);
		try
		{
			JSONStringToProtocol jsonLua = new JSONStringToProtocol(message);

			String rootKey = jsonLua.getRootKey();
			if( rootKey != null )
			{
				if( rootKey.matches("^[_0-9A-Za-z]+$") )
				{
					String fileName = "_" + rootKey.toLowerCase() + ".lua";
					lua.run(fileName, jsonLua.getRootValue() );
				}
			}
		}
		catch (IOException e)
		{
			Log.debug(e.getMessage());
			this.session.close();
		}
	}

	@OnWebSocketClose
	synchronized public void onClose(int statusCode, String reason)
	{
		CS.hubManager.leaveFromAllHubs(session);
		CS.clientManager.remove(this);
		getLua().cleanup();

		Log.debug("Disconnected from %s, %s", CSSessionSupport.getRemoteAddress(session), reason);
	}
}
