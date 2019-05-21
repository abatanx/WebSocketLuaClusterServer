/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.server;

import java.util.*;

public class ClientManager
{
	private Set<WebSockMain> clients = Collections.synchronizedSet(new HashSet<>());
	private ClientManagerDelegate delegate;

	public ClientManager(ClientManagerDelegate delegate)
	{
		this.delegate = delegate;
	}

	public int size()
	{
		return clients.size();
	}

	public void add(WebSockMain ws)
	{
		int before = clients.size();
		clients.add(ws);
		if( before != clients.size() ) delegate.onSessionChanged();
	}

	public void remove(WebSockMain ws)
	{
		int before = clients.size();
		clients.remove(ws);
		if( before != clients.size() ) delegate.onSessionChanged();
	}

	public WebSockMain[] getClients()
	{
		return clients.toArray(new WebSockMain[0]);
	}
}
