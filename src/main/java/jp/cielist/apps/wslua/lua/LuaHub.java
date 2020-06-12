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
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

public class LuaHub extends ZeroArgFunction
{
	private Session session;
	private Hub hub;

	public LuaHub(Session session, Hub hub)
	{
		Log.debug("Activating Hub module, Hub %d ...", CS.hubManager.getId(hub));
		this.session = session;
		this.hub = hub;
	}

	public LuaValue call()
	{
		LuaValue library = tableOf();
		library.set("id", new _instance_methods_._id(hub));
		library.set("join", new _instance_methods_.join(hub, session));
		library.set("leave", new _instance_methods_.leave(hub, session));
		library.set("count", new _instance_methods_.count(hub));
		library.set("members", new _instance_methods_.members(hub));
		library.set("isMember", new _instance_methods_.isMember(hub, session));
		return library;
	}

	static class _instance_methods_
	{
		static class _id extends OneArgFunction
		{
			private Hub hub;
			public _id(Hub hub)
			{
				this.hub = hub;
			}

			public LuaValue call(LuaValue self)
			{
				return LuaValue.valueOf(CS.hubManager.getId(hub));
			}
		}

		static class join extends TwoArgFunction
		{
			private Hub hub;
			private Session session;
			private join(Hub hub, Session session)
			{
				this.hub = hub;
				this.session = session;
			}

			public LuaValue call(LuaValue self, LuaValue arg)
			{
				hub.join(session, arg);
				return LuaValue.valueOf(true);
			}
		}

		static class leave extends TwoArgFunction
		{
			private Hub hub;
			private Session session;
			private leave(Hub hub, Session session)
			{
				this.hub = hub;
				this.session = session;
			}

			public LuaValue call(LuaValue self, LuaValue arg)
			{
				hub.leave(session);
				return LuaValue.valueOf(true);
			}
		}

		static class isMember extends TwoArgFunction
		{
			private Hub hub;
			private Session session;
			private isMember(Hub hub, Session session)
			{
				this.hub = hub;
				this.session = session;
			}

			public LuaValue call(LuaValue self, LuaValue arg)
			{
				return LuaValue.valueOf(hub.isMember(session));
			}
		}

		static class count extends OneArgFunction
		{
			private Hub hub;
			private count(Hub hub)
			{
				this.hub = hub;
			}

			public LuaValue call(LuaValue self)
			{
				return LuaValue.valueOf(hub.count());
			}
		}

		static class members extends OneArgFunction
		{
			private Hub hub;
			private members(Hub hub)
			{
				this.hub = hub;
			}

			public LuaValue call(LuaValue self)
			{
				int i = 0;
				LuaValue result = tableOf();
				for( Object o : hub.getSessions() )
				{
					if( o instanceof LuaValue ) result.set(i+1, (LuaValue) o);
					i ++;
				}
				return result;
			}
		}
	}
}
