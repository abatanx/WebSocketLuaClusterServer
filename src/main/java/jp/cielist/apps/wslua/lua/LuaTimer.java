/**
 * WebSocket-Lua-ClusterServer
 * Copyright (C) 2017 CIEL, K.K., Interfair laboratory
 * ALL RIGHTS RESERVED.
 * @license: MIT
 **/

package jp.cielist.apps.wslua.lua;

import jp.cielist.apps.wslua.common.Log;
import jp.cielist.apps.wslua.server.CS;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class LuaTimer extends TwoArgFunction
{
	private Timer timer;

	public LuaTimer()
	{
		Log.debug("Activating Timer module...");
		timer = null;
	}

	public LuaValue call(LuaValue modname, LuaValue env)
	{
		LuaValue library = tableOf();
		library.set("new", new _new());
		env.set("Timer", library);
		return library;
	}

	/**
	 * Timer.new()
	 */
	class _new extends ZeroArgFunction
	{
		public LuaValue call()
		{
			return (new _instance_methods_()).init();
		}
	}

	class _instance_methods_
	{
		LuaValue init()
		{
			CS.timerManager.add(timer = new Timer());

			Log.debug("Created timer instance: %s", timer.toString());

			LuaValue library = tableOf();
			library.set("once",     new _instance_methods_.once());
			library.set("periodic", new _instance_methods_.periodic());
			library.set("date",		new _instance_methods_.date());
			library.set("stop",     new _instance_methods_.stop());
			return library;
		}

		class InternalTimerTask extends TimerTask
		{
			private LuaValue self;
			private LuaValue callbackFunc;

			private InternalTimerTask(LuaValue self, LuaValue callbackFunc)
			{
				this.self = self;
				this.callbackFunc = callbackFunc;
			}

			@Override
			public void run()
			{
				try
				{
					callbackFunc.invoke(self);
				}
				catch(Exception e)
				{
					Log.error("LuaTimer invoke failed: %s", e.getMessage());
				}
			}
		}

		/**
		 * timer = Timer.new()
		 * timer:once(delay[ms], callbackFunction(self))
		 */
		class once extends ThreeArgFunction
		{
			public LuaValue call(LuaValue self, LuaValue delay, LuaValue callbackFunc)
			{
				timer.schedule(new InternalTimerTask(self, callbackFunc), delay.tolong());
				self.set("callback", callbackFunc);
				return LuaValue.valueOf(true);
			}
		}

		/**
		 * timer = Timer.new()
		 * timer:periodic(period[ms], callbackFunction(self))
		 */
		class periodic extends ThreeArgFunction
		{
			public LuaValue call(LuaValue self, LuaValue delay, LuaValue callbackFunc)
			{
				timer.schedule(new InternalTimerTask(self, callbackFunc), delay.tolong(), delay.tolong());
				self.set("callback", callbackFunc);
				return LuaValue.valueOf(true);
			}
		}

		/**
		 * timer = Timer.new()
		 * timer:date(date, callbackFunction(self))
		 */
		class date extends ThreeArgFunction
		{
			public LuaValue call(LuaValue self, LuaValue date, LuaValue callbackFunc)
			{
				try
				{
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date fireDate = dateFormat.parse(date.tojstring());

					timer.schedule(new InternalTimerTask(self, callbackFunc), fireDate);
					self.set("callback", callbackFunc);
					return LuaValue.valueOf(true);
				}
				catch(ParseException e)
				{
					Log.debug("Can't parse string of date.");
					return LuaValue.valueOf(false);
				}
			}
		}

		/**
		 * timer = Timer.new()
		 * timer:stop()
		 */
		class stop extends OneArgFunction
		{
			public LuaValue call(LuaValue self)
			{
				Log.debug("Dispose and cancelling timer instance: %s", timer.toString());

				CS.timerManager.remove(timer);
				timer.cancel();
				timer = null;
				return LuaValue.valueOf(true);
			}
		}
	}
}
