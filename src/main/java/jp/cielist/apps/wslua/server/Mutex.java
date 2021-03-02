package jp.cielist.apps.wslua.server;

public class Mutex
{
	public final Object luaLock = new Object();
	public int ID = 0;

	synchronized public int getMutexID()
	{
		return ++ID;
	}
}
