/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 *
 * @license: MIT
 **/

package jp.cielist.apps.wslua.lua;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jp.cielist.apps.wslua.common.Log;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;

public class LuaFirebaseAuthentication extends ZeroArgFunction
{
	public LuaFirebaseAuthentication()
	{
		Log.debug( "Activating Firebase Authentication module..." );
	}

	public LuaValue call()
	{
		LuaValue library = tableOf();
		library.set( "verify", new verify() );
		return library;
	}

	class verify extends OneArgFunction
	{
		public LuaValue call( LuaValue token )
		{
			try
			{
				String s;

				FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken( token.toString() );
				LuaValue result = tableOf();
				result.set( "uid", ( s = decodedToken.getUid() ) != null ? LuaValue.valueOf( s ) : LuaValue.NIL );
				result.set( "email", ( s = decodedToken.getEmail() ) != null ? LuaValue.valueOf( s ) : LuaValue.NIL );
				result.set( "name", ( s = decodedToken.getName() ) != null ? LuaValue.valueOf( s ) : LuaValue.NIL );
				result.set( "picture", ( s = decodedToken.getPicture() ) != null ? LuaValue.valueOf( s ) : LuaValue.NIL );
				result.set( "issuer", ( s = decodedToken.getIssuer() ) != null ? LuaValue.valueOf( s ) : LuaValue.NIL );
				result.set( "tenantId", ( s = decodedToken.getTenantId() ) != null ? LuaValue.valueOf( s ) : LuaValue.NIL );
				result.set( "isEmailVerified", LuaValue.valueOf( decodedToken.isEmailVerified() ) );

				return result;

			}
			catch ( FirebaseAuthException e )
			{
				e.printStackTrace();
			}
			return LuaValue.FALSE;
		}
	}
}
