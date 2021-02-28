--
-- hub.lua
--
Core.Events.OnJoin = function(ws)
    print("--> Join")
end

Core.Events.OnJoined = function(ws)
    print("--> Joined")
end

Core.Events.OnLeave = function(ws)
    print("--> Leave")
end

Core.Events.OnLeft = function(ws)
    print("--> Left")
end

Core.Events.OnClose = function()
    print("--> Close")
end
