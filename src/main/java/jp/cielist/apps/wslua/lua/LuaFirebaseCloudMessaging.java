/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 *
 * @license: MIT
 **/

package jp.cielist.apps.wslua.lua;

import com.google.firebase.messaging.*;
import jp.cielist.apps.wslua.common.Log;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import java.util.*;

public class LuaFirebaseCloudMessaging extends ZeroArgFunction
{
	final static int COLLECTIVELY_COUNT = 500;

	public LuaFirebaseCloudMessaging()
	{
		Log.debug( "Activating Firebase Cloud Messaging module..." );
	}

	public LuaValue call()
	{
		LuaValue library = tableOf();
		library.set( "send", new send() );
		return library;
	}

	class send extends TwoArgFunction
	{
		public LuaValue call( LuaValue fcmTokens, LuaValue fcmMessage )
		{
			if ( !fcmTokens.istable() ) return LuaValue.FALSE;
			if ( !fcmMessage.istable() ) return LuaValue.FALSE;

			HashMap<String, String> fcmPayload = new HashMap<>();
			for ( LuaValue lk : ( (LuaTable) fcmMessage ).keys() )
			{
				String k = lk.toString();
				String v = ( (LuaTable) fcmMessage ).get( k ).toString();
				fcmPayload.put( k, v );
			}

			ArrayList<String> tokens = new ArrayList<String>();
			int l = fcmTokens.length();
			for ( int i = 0; i < l ; i++ ) tokens.add( fcmTokens.get( i + 1 ).toString() );

			LuaTable result = LuaTable.tableOf();

			for ( int startIndex = 0; startIndex < l ; startIndex += LuaFirebaseCloudMessaging.COLLECTIVELY_COUNT )
			{
				int endIndex = Integer.min( startIndex + LuaFirebaseCloudMessaging.COLLECTIVELY_COUNT, l );

				List<String> subList = tokens.subList( startIndex, endIndex );

				MulticastMessage message = MulticastMessage.builder().putAllData( fcmPayload ).addAllTokens( subList ).build();

				try
				{
					BatchResponse response = FirebaseMessaging.getInstance().sendMulticast( message );

					int index = 0;
					for ( SendResponse res : response.getResponses() )
					{
						String tokenId = subList.get(index);
						result.set( tokenId, LuaValue.valueOf( res.isSuccessful() ) );
						index ++;
					}
				}
				catch ( FirebaseMessagingException e )
				{
					Log.error( e.getMessage() );
				}
			}

			return result;
		}
	}
}
