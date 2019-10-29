package jp.cielist.apps.wslua.lua;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

public class JSONStringToProtocol extends JSONStringToValue
{
	private LuaValue rootKey   = LuaValue.NIL;
	private LuaValue rootValue = LuaValue.NIL;

	public JSONStringToProtocol(String json)
	{
		super(json);

		LuaValue k = LuaValue.NIL;

		if( result.istable() )
		{
			int m = 0;
			while( true )
			{
				Varargs n = result.next(k);
				if( (k = n.arg1()).isnil() ) break;
				LuaValue v = n.arg(2);

				if( rootKey.isnil() )
				{
					rootKey   = k;
					rootValue = v;
				}
				m ++;
			}

			// Number of key = 0 or 2..
			if( m != 1 )
			{
				rootKey   = LuaValue.NIL;
				rootValue = LuaValue.NIL;
			}
		}
	}

	public String getRootKey()
	{
		return !rootKey.isnil() && rootKey.isstring() ? rootKey.tojstring() : null;
	}

	public LuaValue getRootValue()
	{
		return rootValue;
	}
}
