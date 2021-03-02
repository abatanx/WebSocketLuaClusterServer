--
--
--
if  Core.Server.getAppProperty("app.admin.user") ~= nil     and Core.Request.user == Core.Server.getAppProperty("app.admin.user") and
		Core.Server.getAppProperty("app.admin.password") ~= nil and Core.Request.password == Core.Server.getAppProperty("app.admin.password") then
	print("Administrative mode")
	Core.Server.shared().addMonitor(Core.WebSocket.new())
end
