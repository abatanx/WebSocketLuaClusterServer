interface WSEvents {
    onChangeConnection?: (sender:WS, isConnected: boolean) => void
    onMessage?: (sender:WS, data:any) => void
    onConnectingError?: (sender:WS) => void
}

class WSState {
    public isConnected : boolean

    constructor() {
        this.isConnected = false
    }
}

export class WS {
    private ws: WebSocket
    private serverURL: string

    private state : WSState
    private events : WSEvents

    constructor(serverURL: string, events?:WSEvents) {
        this.serverURL = serverURL
        this.state = new WSState()
        if( events ) this.events = events
    }

    public connect() {
        this.ws = new WebSocket(this.serverURL)
        this.isConnected = false

        this.ws.onopen = ev => {
            this.isConnected = true
        }

        this.ws.onclose = ev => {
            this.isConnected = false
        }

        this.ws.onerror = ev => {
            if( this.ws.readyState === WebSocket.CONNECTING ||
                this.ws.readyState === WebSocket.CLOSED )
            {
                // CONNECTING/CLOSED時にエラーが発生した場合。また open にもなっていない。
                if( this.events.onConnectingError ) this.events.onConnectingError(this)
            }
        }

        this.ws.onmessage = ev => {
            let s = JSON.parse(ev.data)
            if( s && s instanceof Object && !(s instanceof Array) )
            {
                if( this.events.onMessage ) this.events.onMessage(this, s)
            }
        }
    }

    /**
     * Getter & Setter
     */
    get isConnected() : boolean {
        return this.state.isConnected
    }
    set isConnected(flag) {
        if( this.state.isConnected !== flag )
        {
            this.state.isConnected = flag
            if( this.events.onChangeConnection )
            {
                this.events.onChangeConnection(this, this.state.isConnected)
            }
        }
    }

    /**
     * Methods
     */
    public close()
    {
        this.ws.close()
    }
}
