monitoring = {}

Core.Events.OnSessionChanged = function()
    print("-- OnSessionChanged")
end

Core.Events.OnSessionJoin = function(session)
    print("-- OnSessionJoin")
    sendMonitor()
end

Core.Events.OnSessionLeave = function(session)
    print("-- OnSessionLeave")
    sendMonitor()
end

Core.Events.OnHubStart = function(session)
    print("-- OnHubStart")
    sendMonitor()
end

Core.Events.OnHubLeave = function(session)
    print("-- OnSessionLeave")
    sendMonitor()
end

Core.Events.OnHubSessionJoin = function(hub, session)
    print("-- OnHubSessionJoin")
    sendMonitor()
end

Core.Events.OnHubSessionLeave = function(hub, session)
    print("-- OnHubSessionLeave")
    sendMonitor()
end

sendMonitor = function()
    local hubs = {}

    for i,hub in ipairs(Core.Server.allHubs()) do
        local members = {}
        for m,ws in ipairs(hub:members()) do
            table.insert(members, ws.ID)
        end
        table.insert(hubs, {id = hub.Core.ID, hubId = hub:id(), members = members})
    end

    local sessions = {}
    for i,session in ipairs(Core.Server.allSessions()) do
        table.insert(sessions, {id = session.Core.ID, ip = (session.Core.WebSocket.new()).getRemoteAddress()})
    end

    for i,adminws in ipairs(monitoring) do
        if adminws:isOpen() then
            adminws:send(Core.JSON.stringify({monitor = {hubs = hubs, sessions = sessions} }))
        end
    end
end

addMonitor = function(ws)
    table.insert(monitoring, ws)
    sendMonitor()
end

removeMonitor = function(ws)
    for i,ws in ipairs(monitoring) do
        if ws.ID == ws.ID then
            table.remove(monitoring, i)
            return true
        end
    end
    return false
end
