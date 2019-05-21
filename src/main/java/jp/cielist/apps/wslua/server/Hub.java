/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.server;

import org.eclipse.jetty.websocket.api.Session;
import java.util.HashMap;

public class Hub
{
	private HashMap<Session,Object> members;

	public Hub()
	{
		members = new HashMap<Session,Object>();
	}

	public Object[] getSessions()
	{
		return members.values().toArray();
	}

	public boolean isMember(Session session)
	{
		return members.containsKey(session);
	}

	public boolean join(Session session, Object object)
	{
		if( members.containsKey(session) ) return false;
		members.put(session, object);
		return true;
	}

	public boolean leave(Session session)
	{
		if( !members.containsKey(session) ) return false;
		members.remove(session);
		return true;
	}

	public int count()
	{
		return members.size();
	}
}
