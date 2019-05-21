/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.server;
import jp.cielist.apps.wslua.common.Log;
import jp.cielist.apps.wslua.common.ProtocolString;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.ArrayList;

public class Client
{
	public Session connection;
	public DB db;

	public void send(Object... args) throws IOException
	{
		ArrayList<String> params = new ArrayList<>();
		for(Object arg : args) params.add(arg.toString());

		String encoded = ProtocolString.encode(params.toArray(new String[0]));

		Log.sendLog(encoded);
		connection.getRemote().sendString(encoded);
	}
}
