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
import jp.cielist.apps.wslua.server.WebSockMain;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

public class LuaServer extends ZeroArgFunction
{
	public LuaServer()
	{
		Log.debug("Activating Hubs module...");
	}

	public LuaValue call()
	{
		LuaValue library = tableOf();
		library.set("getAllHubs", new getAllHubs());
		library.set("getAllWebSockets", new getAllWebSockets());
		return library;
	}

	static class getAllHubs extends ZeroArgFunction
	{
		public LuaValue call()
		{
			int i = 0;
			LuaValue result = tableOf();
			for( Hub hub : CS.hubManager.hubs() )
			{
				result.set(i+1, hub.getLuaEnv().getLua());
				i ++;
			}
			return result;
		}
	}

	static class getAllWebSockets extends ZeroArgFunction
	{
		public LuaValue call()
		{
			int i = 0;
			LuaValue result = tableOf();
			for( WebSockMain ws : CS.clientManager.getClients() )
			{
				result.set(i+1, ws.getLuaEnv().getLua());
				i ++;
			}
			return result;
		}
	}
}
