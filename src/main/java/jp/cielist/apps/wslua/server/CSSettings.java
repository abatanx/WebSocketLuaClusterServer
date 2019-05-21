/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.server;

import jp.cielist.apps.wslua.common.Log;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

public class CSSettings
{
	public int port;
	public String appName, appVersion;
	public String jdbcDriver, dbDsn, dbUser, dbPassword;
	public String hashKey, passwordHashKey;
	public String luaDir;
	public String luaBootstrapFile;
	public String luaPackagePath;

	public CSSettings()
	{
		init();
	}

	private void init()
	{
		appName = "";
		appVersion = "";
		port = 9988;
		jdbcDriver = "";
		dbDsn = "";
		dbUser = "";
		dbPassword = "";
		hashKey = "";
		passwordHashKey = "";
		luaDir = "";
		luaBootstrapFile = "";
		luaPackagePath = "?.lua";
	}

	public void load(String paramFile) throws IllegalArgumentException,IOException
	{
		init();

		// プロパティ読み込み
		Properties property = new Properties();
		property.load(new FileInputStream(paramFile));

		// 稼働アプリ名
		appName = property.getProperty("app.name", "");
		appVersion = property.getProperty("app.version", "");

		// WS待ち受けポート番号
		port = Integer.parseInt(property.getProperty("server.port","9988"));

		// DB接続
		jdbcDriver = property.getProperty("jdbc.driver", "");
		dbDsn = property.getProperty("db.dsn", "");
		dbUser = property.getProperty("db.user", "");
		dbPassword = property.getProperty("db.password", "");

		hashKey = property.getProperty("hash.key", "");
		passwordHashKey = property.getProperty("hash.password", "");

		// Lua
		luaDir = property.getProperty("lua.dir","");
		if( !luaDir.equals("") )
		{
			if( !luaDir.endsWith("/") ) luaDir = luaDir + "/";
		}
		luaBootstrapFile = property.getProperty("lua.bootstrap", "");
		luaPackagePath   = property.getProperty("lua.package.path" , "?.lua");

		description();
	}

	private String hiddenString(String value)
	{
		String pw = "";
		for(int i=0; i<value.length(); i++) pw = pw + "*";
		return pw;
	}

	public void description()
	{
		Log.info("\t-------- app");
		Log.info("\tapp.name         : %s", appName);
		Log.info("\tapp.version      : %s", appVersion);
		Log.info("\tserver.port      : %d", port);
		Log.info("\t-------- LuaDB");
		Log.info("\tjdbc.driver      : %s", jdbcDriver);
		Log.info("\tdb.dsn           : %s", dbDsn);
		Log.info("\tdb.user          : %s", dbUser);
		Log.info("\tdb.password      : %s", hiddenString(dbPassword));
		Log.info("\t-------- secrets");
		Log.info("\thash.key         : %s", hiddenString(hashKey));
		Log.info("\thash.password    : %s", hiddenString(passwordHashKey));
		Log.info("\t-------- lua");
		Log.info("\tlua.dir          : %s", luaDir);
		Log.info("\tlua.bootstrap    : %s", luaBootstrapFile);
		Log.info("\tlua.package.path : %s", luaPackagePath);
		Log.info("\t--------");
	}

	final static String GeneralHash(String hashKey, String str)
	{
		String algo = "HmacSHA256";
		StringBuilder sb;
		try
		{
			SecretKeySpec sk = new SecretKeySpec(hashKey.getBytes(), algo);
			Mac mac = Mac.getInstance(algo);
			mac.init(sk);

			byte[] mac_bytes = mac.doFinal(str.getBytes());

			sb = new StringBuilder(2 * mac_bytes.length);
			for(byte b: mac_bytes)
			{
				sb.append(String.format("%02x", b&0xff) );
			}
		}
		catch (NoSuchAlgorithmException e)
		{
			Log.debug("Crypto error: %s", e.getMessage());
			e.printStackTrace();
			return "";
		}
		catch (InvalidKeyException e)
		{
			Log.debug("Crypto error: %s", e.getMessage());
			e.printStackTrace();
			return "";
		}

		return sb.toString();
	}

	public String passwordHash(String str)
	{
		return GeneralHash(passwordHashKey, str);
	}

	public String hash(String str)
	{
		return GeneralHash(hashKey, str);
	}

}
