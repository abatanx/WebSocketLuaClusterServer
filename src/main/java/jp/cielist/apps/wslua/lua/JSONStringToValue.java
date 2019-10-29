package jp.cielist.apps.wslua.lua;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class JSONStringToValue
{
	protected LuaValue result = LuaValue.NIL;

	public JSONStringToValue(String json)
	{
		ObjectMapper mapper = new ObjectMapper();
		try
		{
			JsonNode root = mapper.readTree(json);
			result  = parse(root);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public LuaValue getLuaValue()
	{
		return result;
	}

	private LuaValue parse(JsonNode root) throws Exception
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
					luaObject.set(obj.getKey(), parse(obj.getValue()));
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
					luaObject.set(i++, parse(elm));
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
