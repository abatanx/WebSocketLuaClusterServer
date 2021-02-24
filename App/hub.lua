--
-- hub.lua
--
onJoin = function(ws)
    print("--> Join")
end

onJoined = function(ws)
    print("--> Joined")
end

onLeave = function(ws)
    print("--> Leave")
end

onLeft = function(ws)
    print("--> Left")
end

onClose = function()
    print("--> Close")
end
