/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.server;

import jp.cielist.apps.wslua.common.Log;
import org.eclipse.jetty.websocket.api.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HubManager
{
	private HashMap<Integer,Hub> hubs = new HashMap<Integer, Hub>();
	private ClientManagerDelegate delegate;

	public HubManager(ClientManagerDelegate delegate)
	{
		this.delegate = delegate;
	}

	public ClientManagerDelegate getDelegate()
	{
		return delegate;
	}

	/**
	 * Add Hub
	 * */
	public void add(Integer key, Hub hub)
	{
		if( hubs.get(key) == null )
		{
			hubs.put(key,hub);
			delegate.onHubStart(hub);
		}
	}

	/**
	 * Remove Hub
	 * */
	public void remove(Integer key)
	{
		Hub removingHub;
		if( (removingHub = hubs.get(key)) != null )
		{
			hubs.remove(key);
			delegate.onHubEnd(removingHub);
		}
	}

	/**
	 * Get Hub
	 */
	public Hub get(Integer key)
	{
		return hubs.get(key);
	}

	/**
	 * Get Hub-ID
	 */
	public int getId(Hub hub)
	{
		for (Map.Entry<Integer, Hub> entry : hubs.entrySet())
		{
			if( entry.getValue() == hub ) return entry.getKey();
		}
		return 0;
	}

	/**
	 *
	 */
	public Hub[] hubs()
	{
		return hubs.values().toArray(new Hub[0]);
	}

	/**
	 * RemoveKillMembers
	 */
	public void leaveFromAllHubs(Session session)
	{
		Log.debug("HubManager: Checking all hubs at leave session.");

		List<Integer> removeIds = new ArrayList<Integer>();

		for (Map.Entry<Integer, Hub> entry : hubs.entrySet())
		{
			if( entry.getValue().isMember(session) )
			{
				Log.debug("HubManager: Removing %s from Hub %d",
					session.getRemoteAddress().toString(),
						entry.getKey().intValue());
				entry.getValue().leave(session);
			}

			if( entry.getValue().count() == 0 )
			{
				Log.debug("HubManager: Hub %d is empty, auto closed.",
						entry.getKey().intValue());
				entry.getValue().close();
				removeIds.add(entry.getKey());
			}
		}

		for(Integer removeId : removeIds) remove(removeId);
	}

	/**
	 * RemoveEmptyHub
	 */
	public void checkAndRemoveEmptyHub()
	{
		Log.debug("HubManager: Checking all empty hubs.");
		for (Map.Entry<Integer, Hub> entry : hubs.entrySet())
		{
			if( entry.getValue().count()==0 )
			{
				Log.debug("HubManager: Hub %d auto removed",
					entry.getKey().intValue());
				entry.getValue().close();
				remove(entry.getKey());
			}
		}
	}
}
