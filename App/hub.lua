--
-- hub.lua
--
Core.Events.onJoin = function(ws)
    print("--> Join")
end

Core.Events.onJoined = function(ws)
    print("--> Joined")
end

Core.Events.onLeave = function(ws)
    print("--> Leave")
end

Core.Events.onLeft = function(ws)
    print("--> Left")
end

Core.Events.onClose = function()
    print("--> Close")
end
