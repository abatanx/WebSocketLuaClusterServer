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

public class LuaHub extends TwoArgFunction
{
	private Session session;

	public LuaHub(Session session)
	{
		Log.debug("Activating Hub module...");
		this.session = session;
	}

	public LuaValue call(LuaValue modname, LuaValue env)
	{
		LuaValue library = tableOf();
		library.set("new", new _new(this.session));
		library.set("hubs", new hubs(this.session));
		env.set("Hub", library);
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
			return _instance_methods_.init(hub, session);
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
				result.set(i+1, _instance_methods_.init(hub, session));
				i ++;
			}
			return result;
		}
	}

	static class _instance_methods_
	{
		static LuaValue init(Hub hub, Session session)
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
