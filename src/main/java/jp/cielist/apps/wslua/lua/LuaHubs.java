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
	public LuaHubs()
	{
		Log.debug("Activating Hubs module...");
	}

	public LuaValue call()
	{
		LuaValue library = tableOf();
		library.set("new", new _new());
		library.set("all", new all());
		return library;
	}

	static class _new extends OneArgFunction
	{
		public LuaValue call(LuaValue arg2)
		{
			int key = arg2.toint();
			Hub hub = CS.hubManager.get(key);
			if (hub == null)
			{
				hub = new Hub(CS.hubManager.getDelegate());
				hub.initLuaEnv();
				CS.hubManager.add(key, hub);
			}
			return hub.getLuaEnv().getLua();
		}
	}

	static class all extends ZeroArgFunction
	{
		public LuaValue call()
		{
			Hub[] hubs = CS.hubManager.hubs();
			int i = 0;
			LuaValue result = tableOf();
			for( Hub hub : hubs )
			{
				result.set(i+1, hub.getLuaEnv().getLua());
				i ++;
			}
			return result;
		}
	}
}
