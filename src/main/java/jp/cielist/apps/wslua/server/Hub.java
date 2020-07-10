/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.server;

import org.eclipse.jetty.websocket.api.Session;
import org.luaj.vm2.LuaValue;
import java.util.HashMap;

public class Hub
{
	private HashMap<Session,LuaValue> members;
	private LuaEnvHub lua;

	public Hub()
	{
		members = new HashMap<Session,LuaValue>();
	}

	public void initLuaEnvHub()
	{
		lua = new LuaEnvHub(this);
	}

	public Object[] getSessions()
	{
		return members.values().toArray();
	}

	public boolean isMember(Session session)
	{
		return members.containsKey(session);
	}

	public boolean join(Session session, LuaValue object)
	{
		if( members.containsKey(session) ) return false;
		lua.join(object);
		members.put(session, object);
		lua.joined(object);
		return true;
	}

	public boolean leave(Session session)
	{
		if( !members.containsKey(session) ) return false;
		LuaValue object = members.get(session);
		lua.leave(object);
		members.remove(session);
		lua.left(object);
		return true;
	}

	public void close()
	{
		lua.close();
	}

	public int count()
	{
		return members.size();
	}
}
