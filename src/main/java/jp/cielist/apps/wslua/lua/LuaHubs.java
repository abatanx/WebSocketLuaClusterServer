/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.lua;

import jp.cielist.apps.wslua.common.Log;
import jp.cielist.apps.wslua.server.CS;
import jp.cielist.apps.wslua.server.Hub;
import org.eclipse.jetty.websocket.api.Session;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

public class LuaHubs extends ZeroArgFunction
{
	private Session session;

	public LuaHubs(Session session)
	{
		Log.debug("Activating Hubs module...");
		this.session = session;
	}

	public LuaValue call()
	{
		LuaValue library = tableOf();
		library.set("new", new _new(this.session));
		library.set("hubs", new hubs(this.session));
		return library;
	}

	static class _new extends OneArgFunction
	{
		private Session session;
		private _new(Session session)
		{
			this.session = session;
		}

		public LuaValue call(LuaValue arg2)
		{
			int key = arg2.toint();
			Hub hub = CS.hubManager.get(key);
			if (hub == null)
			{
				hub = new Hub();
				CS.hubManager.add(key, hub);
			}
			return (new LuaHub(session, hub)).call();
		}
	}

	static class hubs extends ZeroArgFunction
	{
		private Session session;
		private hubs(Session session)
		{
			this.session = session;
		}

		public LuaValue call()
		{
			Hub[] hubs = CS.hubManager.hubs();
			int i = 0;
			LuaValue result = tableOf();
			for( Hub hub : hubs )
			{
				result.set(i+1, (new LuaHub(session, hub)).call());
				i ++;
			}
			return result;
		}
	}
}
