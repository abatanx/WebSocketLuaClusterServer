/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.server;

import jp.cielist.apps.wslua.common.Log;
import org.eclipse.jetty.websocket.api.Session;
import org.luaj.vm2.LuaValue;
import java.util.HashMap;

public class Hub
{
	private HashMap<Session,LuaValue> members;
	ClientManagerDelegate delegate;
	private LuaEnvHub lua;

	public Hub(ClientManagerDelegate delegate)
	{
		this.delegate = delegate;
		members = new HashMap<Session,LuaValue>();
	}

	public void initLuaEnv()
	{
		lua = new LuaEnvHub(this);
	}

	public LuaEnvHub getLuaEnv()
	{
		return lua;
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
		WebSockMain ws;

		if( members.containsKey(session) ) return false;
		lua.join(object);
		members.put(session, object);
		if( (ws = CS.clientManager.getLuaBySession(session)) != null )
		{
			delegate.onHubSessionJoin(this, ws);
		}
		else
		{
			Log.error("Can't find websocket instance on HubSessionJoin.");
		}
		lua.joined(object);
		return true;
	}

	public boolean leave(Session session)
	{
		WebSockMain ws;

		if( !members.containsKey(session) ) return false;
		LuaValue object = members.get(session);
		lua.leave(object);
		members.remove(session);
		if( (ws = CS.clientManager.getLuaBySession(session)) != null )
		{
			delegate.onHubSessionLeave(this, ws);
		}
		else
		{
			Log.error("Can't find websocket instance on HubSessionLeave.");
		}
		lua.left(object);

		// Can't release hub instance.
		// CS.hubManager.checkAndRemoveEmptyHub();
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
