package jp.cielist.apps.wslua.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class JsonToLuaObject
{
	private LuaValue result = LuaValue.NIL;

	private LuaValue rootKey   = LuaValue.NIL;
	private LuaValue rootValue = LuaValue.NIL;

	public JsonToLuaObject(String json) throws IOException
	{
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(json);
		result  = expand(root);

		int m = 0;
		LuaValue k = LuaValue.NIL, v = LuaValue.NIL ;
		rootKey    = LuaValue.NIL;
		rootValue  = LuaValue.NIL;
		while( true )
		{
			Varargs n = result.next(k);
			if( (k = n.arg1()).isnil() ) break;
			v = n.arg(2);

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

	public LuaValue getLuaValue()
	{
		return result;
	}

	public String getRootKey()
	{
		return !rootKey.isnil() && rootKey.isstring() ? rootKey.tojstring() : null;
	}

	public LuaValue getRootValue()
	{
		return rootValue;
	}

	private LuaValue expand(JsonNode root) throws IOException
	{
		switch(root.getNodeType())
		{
			case OBJECT:
			{
				LuaValue luaObject = LuaValue.tableOf();
				Iterator<Map.Entry<String, JsonNode>> it = root.fields();
				while( it.hasNext() )
				{
					Map.Entry<String, JsonNode> obj = it.next();
					luaObject.set(obj.getKey(), expand(obj.getValue()));
				}
				return luaObject;
			}
			case ARRAY:
			{
				int i = 1;
				LuaValue luaObject = LuaValue.tableOf();
				Iterator<JsonNode> it = root.elements();
				while( it.hasNext() )
				{
					JsonNode elm = it.next();
					luaObject.set(i++, expand(elm));
				}
				return luaObject;
			}
			case BOOLEAN:
				return root.asBoolean() ? LuaValue.TRUE : LuaValue.FALSE;
			case NUMBER:
				return root.isInt() ? LuaValue.valueOf(root.asInt()) : LuaValue.valueOf(root.asDouble());
			case STRING:
				return LuaValue.valueOf(root.asText());
			case BINARY:
				return LuaValue.valueOf(root.binaryValue());
		}
		return LuaValue.NIL;
	}
}
