/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.server;

interface ClientManagerDelegate
{
	public void onSessionChanged();
	public void onSessionJoin(WebSockMain ws);
	public void onSessionLeave(WebSockMain ws);

	public void onHubStart(Hub hub);
	public void onHubEnd(Hub hub);

	public void onHubSessionJoin(Hub hub, WebSockMain ws);
	public void onHubSessionLeave(Hub hub, WebSockMain ws);
}
