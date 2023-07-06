/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.server;

import jp.cielist.apps.wslua.common.Log;
import jp.cielist.apps.wslua.lua.JSONStringToProtocol;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.luaj.vm2.LuaValue;

import java.io.IOException;

@WebSocket
public class WebSockMain
{
	// Socket connection
	private Session session;

	// Lua
	private LuaEnv lua;

	public LuaEnv getLuaEnv()
	{
		return lua;
	}

	public Session getSession()
	{
		return session;
	}

	@OnWebSocketConnect
	public void onConnect(Session session)
	{
		synchronized (CS.mutex.luaLock)
		{
			Log.notice("Connected from %s", CSSessionSupport.getRemoteAddress(session));

			this.session = session;

			lua = new LuaEnv(session, false, false);

			CS.clientManager.add(this);
		}
	}

	@OnWebSocketMessage
	public void onText(String message) {
		synchronized (CS.mutex.luaLock)
		{
			Log.receiveLog(message);
			try
			{
				String execute = null;

				JSONStringToProtocol jsonLua = new JSONStringToProtocol(message);
				LuaValue data = LuaValue.NIL;

				if( CSConfig.settings.jsonKey == null )
				{
					// {"exec":{}} pattern
					execute = jsonLua.getRootKey();
					data = jsonLua.getRootValue();
				}
				else
				{
					// {"id":"exec",{}} pattern
					execute = jsonLua.getLuaValue().get( CSConfig.settings.jsonKey ).toString();
					data = jsonLua.getLuaValue();
				}

				if( execute != null )
				{
					if (execute.matches("^[_0-9A-Za-z]+$"))
					{
						String fileName = "_" + execute.toLowerCase() + ".lua";
						lua.run(fileName, data);
					}
				}
				else
				{
					Log.warning( "Unsupported JSON payload, %s", message);
				}
			}
			catch (IOException e)
			{
				Log.error("Receiving error %s, %s", CSSessionSupport.getRemoteAddress(session), e.getMessage());
				this.session.close();
			}
		}
	}

	@OnWebSocketClose
	public void onClose(int statusCode, String reason)
	{
		synchronized (CS.mutex.luaLock)
		{
			CS.hubManager.leaveFromAllHubs(session);
			CS.clientManager.remove(this);
			getLuaEnv().cleanup();

			Log.notice("Disconnected from %s, %s", CSSessionSupport.getRemoteAddress(session), reason);
		}
	}

	@OnWebSocketError
	public void onError(Throwable cause)
	{
		if (this.session != null) {
			Log.notice("WebSocket error %s, %s", CSSessionSupport.getRemoteAddress(session), cause.getMessage());
		}
	}



}
