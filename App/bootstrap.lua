--
-- bootstrap.lua
--
Core.Events.OnSessionChanged = function()
    print("-- OnSessionChanged")
end

Core.Events.OnSessionJoin = function(session)
    print("-- OnSessionJoin")
    local w = session.Core.WebSocket.new()
    print(w:getRemoteAddress())

end

Core.Events.OnSessionLeave = function(session)
    print("-- OnSessionLeave")
    local w = session.Core.WebSocket.new()
    print(w:getRemoteAddress())
end

Core.Events.OnHubStart = function(session)
    print("-- OnHubStart")
end

Core.Events.OnHubLeave = function(session)
    print("-- OnSessionLeave")
    local w = session.Core.WebSocket.new()
    print(w:getRemoteAddress())
end

Core.Events.OnHubSessionJoin = function(hub, session)
    print("-- OnHubSessionJoin")
end

Core.Events.OnHubSessionLeave = function(hub, session)
    print("-- OnHubSessionLeave")
end

