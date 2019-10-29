/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.lua;

import jp.cielist.apps.wslua.common.Log;
import jp.cielist.apps.wslua.common.ProtocolString;
import org.eclipse.jetty.websocket.api.Session;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;

import java.io.IOException;
import java.util.ArrayList;

public class LuaWebSocket extends ZeroArgFunction
{
	private Session session;
	public LuaWebSocket(Session session)
	{
		Log.debug("Activating WebSocket module...");
		this.session = session;
	}

	public LuaValue call()
	{
		LuaValue library = tableOf();
		library.set("new", new _new());
		return library;
	}

	class _new extends ZeroArgFunction
	{
		public LuaValue call()
		{
			LuaValue library = tableOf();
			library.set("send", new send());
			library.set("close", new close());
			library.set("disconnect", new disconnect());
			library.set("setIdleTimeout", new setIdleTimeout());
			library.set("getIdleTimeout", new getIdleTimeout());
			library.set("getLocalAddress", new getLocalAddress());
			library.set("getRemoteAddress", new getRemoteAddress());
			library.set("getProtocolVersion", new getProtocolVersion());
			library.set("isOpen", new isOpen());
			library.set("isSecure", new isSecure());
			return library;
		}

		class send extends TwoArgFunction
		{
			public LuaValue call(LuaValue self, LuaValue object)
			{
				String str = object.tojstring();
				try
				{
					Log.sendLog(str);
					session.getRemote().sendString(str);
				}
				catch(IOException e)
				{
					return LuaValue.valueOf(false);
				}
				return LuaValue.valueOf(true);
			}
		}

		class setIdleTimeout extends TwoArgFunction
		{
			public LuaValue call(LuaValue self, LuaValue arg)
			{
				session.setIdleTimeout(arg.tolong());
				return LuaValue.TRUE;
			}
		}

		class getIdleTimeout extends OneArgFunction
		{
			public LuaValue call(LuaValue self)
			{
				return LuaValue.valueOf(session.getIdleTimeout());
			}
		}

		class close extends OneArgFunction
		{
			public LuaValue call(LuaValue self)
			{
				session.close();
				return LuaValue.TRUE;
			}
		}

		class disconnect extends OneArgFunction
		{
			public LuaValue call(LuaValue self)
			{
				try
				{
					session.disconnect();
				}
				catch(IOException e)
				{
					Log.debug("%s", e.getMessage());
					return LuaValue.FALSE;
				}
				return LuaValue.TRUE;
			}
		}

		class getLocalAddress extends OneArgFunction
		{
			public LuaValue call(LuaValue self)
			{
				return LuaValue.valueOf(session.getLocalAddress().toString());
			}
		}

		class getRemoteAddress extends OneArgFunction
		{
			public LuaValue call(LuaValue self)
			{
				return LuaValue.valueOf(session.getRemoteAddress().toString());
			}
		}

		class getProtocolVersion extends OneArgFunction
		{
			public LuaValue call(LuaValue self)
			{
				return LuaValue.valueOf(session.getProtocolVersion());
			}
		}

		class isOpen extends OneArgFunction
		{
			public LuaValue call(LuaValue self)
			{
				return LuaValue.valueOf(session.isOpen());
			}
		}

		class isSecure extends OneArgFunction
		{
			public LuaValue call(LuaValue self)
			{
				return LuaValue.valueOf(session.isSecure());
			}
		}
	}
}
