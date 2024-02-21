/*
Parent driver fo Xantech 8 Zone audio
Xantech.com/product?p_id=10761
This driver is to control the Xantech 8 Zone amplifier.
I wrote this diver for personal use. If you decide to use it, do it at your own risk.
No guarantee or liability is accepted for damages of any kind.
for the driver to work it also needs RS232 to Ethernet like this one:
        https://www.aliexpress.com/item/32988953549.html?spm=a2g0o.productlist.0.0.517f5e27r8pql4&algo_pvid=f21f7b9e-0d3b-4920-983c-d9df0da59484&algo_expid=f21f7b9e-0d3b-4920-983c-d9df0da59484-1&btsid=0ab6f83115925263810321337e7408&ws_ab_test=searchweb0_0,searchweb201602_,searchweb201603_
        https://www.amazon.com/USR-TCP232-302-Serial-Ethernet-Converter-Support/dp/B01GPGPEBM/ref=sr_1_6?dchild=1&keywords=RS232+to+Ethernet&qid=1592526464&sr=8-6 or similar
        is been test it too on:
        https://www.amazon.ca/gp/product/B087J9F6LF/ref=ppx_yo_dt_b_asin_title_o02_s00?ie=UTF8&psc=1
        08/11/2020
        this driver also work on a rasberry pi running ser2net.py by Pavel Revak https://github.com/pavelrevak/ser2tcp
        is recomended to daemonize the scrips instruction on the gihub https://github.com/martinezmp3/Hubitat-Xantech-6-zone-controller/blob/master/README.md
        08/12/2020
        compatibility with 2 and 3 amps connected as a chain i dont own a second amp
        08/20/2020
        Parent save source name to be display on dashboard
Jorge Martinez
*/

metadata {
    definition(name: 'Parent Xantech 8 Zone Amp Controller', namespace: 'suresh.kavan', author: 'Jorge Martinez') {
        capability 'Polling'
        capability 'Telnet'
        capability 'Initialize'
        //        capability "Actuator"
        //        capability "Switch"
        //      capability "Sensor"
        //        capability "AudioVolume"
        command 'recreateChildDevices'
        command 'poll'
        command 'forcePoll'
        command 'sendMsg' , ['STRING']
        command 'CloseTelnet'
        command 'setChildzones'
        command 'Unschedule'
    }
    preferences {
        section('Device Settings:')
        {
            input 'IP', 'String', title:'IP of Amp Controller', description: '', required: true, displayDuringSetup: true
            input 'port', 'NUMBER', title:'port of Amp Controller', description: '', required: true, displayDuringSetup: true
            input 'Zone1Name', 'String', title:'Name Of Zone 1', description: '', required: true, defaultValue: 'Zone_1'
            input 'Zone2Name', 'String', title:'Name Of Zone 2', description: '', required: true, defaultValue: 'Zone_2'
            input 'Zone3Name', 'String', title:'Name Of Zone 3', description: '', required: true, defaultValue: 'Zone_3'
            input 'Zone4Name', 'String', title:'Name Of Zone 4', description: '', required: true, defaultValue: 'Zone_4'
            input 'Zone5Name', 'String', title:'Name Of Zone 5', description: '', required: true, defaultValue: 'Zone_5'
            input 'Zone6Name', 'String', title:'Name Of Zone 6', description: '', required: true, defaultValue: 'Zone_6'
            input 'Zone7Name', 'String', title:'Name Of Zone 7', description: '', required: true, defaultValue: 'Zone_7'
            input 'Zone8Name', 'String', title:'Name Of Zone 8', description: '', required: true, defaultValue: 'Zone_8'
            input 'Zone9Name', 'String', title:'Name Of Zone 9', description: '', required: true, defaultValue: 'Zone_9'
            input 'Zone10Name', 'String', title:'Name Of Zone 10', description: '', required: true, defaultValue: 'Zone_10'
            input 'Zone11Name', 'String', title:'Name Of Zone 11', description: '', required: true, defaultValue: 'Zone_11'
            input 'Zone12Name', 'String', title:'Name Of Zone 12', description: '', required: true, defaultValue: 'Zone_12'
            input 'Zone13Name', 'String', title:'Name Of Zone 13', description: '', required: true, defaultValue: 'Zone_13'
            input 'Zone14Name', 'String', title:'Name Of Zone 14', description: '', required: true, defaultValue: 'Zone_14'
            input 'Zone15Name', 'String', title:'Name Of Zone 15', description: '', required: true, defaultValue: 'Zone_15'
            input 'Zone16Name', 'String', title:'Name Of Zone 16', description: '', required: true, defaultValue: 'Zone_16'
            input 'Zone17Name', 'String', title:'Name Of Zone 17', description: '', required: true, defaultValue: 'Zone_17'
            input 'Zone18Name', 'String', title:'Name Of Zone 18', description: '', required: true, defaultValue: 'Zone_18'
            input 'Channel1Name', 'String', title:'Name of channel 1', description: '', required: true, defaultValue: 'Channel1'
            input 'Channel2Name', 'String', title:'Name of channel 2', description: '', required: true, defaultValue: 'Channel2'
            input 'Channel3Name', 'String', title:'Name of channel 3', description: '', required: true, defaultValue: 'Channel3'
            input 'Channel4Name', 'String', title:'Name of channel 4', description: '', required: true, defaultValue: 'Channel4'
            input 'Channel5Name', 'String', title:'Name of channel 5', description: '', required: true, defaultValue: 'Channel5'
            input 'Channel6Name', 'String', title:'Name of channel 6', description: '', required: true, defaultValue: 'Channel6'
            input name: 'logEnable', type: 'bool', title: 'Enable debug logging', defaultValue: true
            input name: 'NumberAmps', type: 'enum', description: '', title: 'Number Amps', options: [[1:'1'], [2:'2'], [3:'3']], defaultValue: 1
            input name: 'PollSchedule', type: 'enum', description: '', title: 'Poll frequency in min', options: [[1:'1'], [2:'5'], [3:'15'], [4:'30']], defaultValue: 1
        // 1, 5, 15 and 30 minites
        }
    }
}

def Unschedule() {
    if (logEnable) log.debug 'Parent unschedule'
    unschedule()
}
def setChildzones() {
    if (logEnable) log.debug 'Parent setChildzones'
    def children = getChildDevices()
    children.each { child->
        child.setZone()
    }
}
def recreateChildDevices() {
    if (logEnable) log.debug 'Parent recreateChildDevices'
    deleteChildren()
    createChildDevices()
}
def createChildDevices() {
    log.debug "Parent createChildDevices ${settings.NumberAmps}"
    addChildDevice('suresh.kavan', 'Child Xantech 8 Zone Amp Controller', 'MP6ZA-child-01', [name: "child-${Zone1Name}", label: "${settings.Zone1Name}", zone: 1, isComponent: false])
    addChildDevice('suresh.kavan', 'Child Xantech 8 Zone Amp Controller', 'MP6ZA-child-02', [name: "child-${Zone2Name}", label: "${settings.Zone2Name}", zone: 2, isComponent: false])
    addChildDevice('suresh.kavan', 'Child Xantech 8 Zone Amp Controller', 'MP6ZA-child-03', [name: "child-${Zone3Name}", label: "${settings.Zone3Name}", zone: 3, isComponent: false])
    addChildDevice('suresh.kavan', 'Child Xantech 8 Zone Amp Controller', 'MP6ZA-child-04', [name: "child-${Zone4Name}", label: "${settings.Zone4Name}", zone: 4, isComponent: false])
    addChildDevice('suresh.kavan', 'Child Xantech 8 Zone Amp Controller', 'MP6ZA-child-05', [name: "child-${Zone5Name}", label: "${settings.Zone5Name}", zone: 5, isComponent: false])
    addChildDevice('suresh.kavan', 'Child Xantech 8 Zone Amp Controller', 'MP6ZA-child-06', [name: "child-${Zone6Name}", label: "${settings.Zone6Name}", zone: 6, isComponent: false])
    if (settings.NumberAmps.toInteger() > 1) {
        if (logEnable) log.debug 'Parent: 2 amp ceating 2nd set of childerens'
        addChildDevice('suresh.kavan', 'Child Xantech 8 Zone Amp Controller', 'MP6ZA-child-21', [name: "child-${Zone7Name}", label: "${settings.Zone7Name}", zone: 21, isComponent: false])
        addChildDevice('suresh.kavan', 'Child Xantech 8 Zone Amp Controller', 'MP6ZA-child-22', [name: "child-${Zone8Name}", label: "${settings.Zone8Name}", zone: 22, isComponent: false])
        addChildDevice('suresh.kavan', 'Child Xantech 8 Zone Amp Controller', 'MP6ZA-child-23', [name: "child-${Zone9Name}", label: "${settings.Zone9Name}", zone: 23, isComponent: false])
        addChildDevice('suresh.kavan', 'Child Xantech 8 Zone Amp Controller', 'MP6ZA-child-24', [name: "child-${Zone10Name}", label: "${settings.Zone10Name}", zone: 24, isComponent: false])
        addChildDevice('suresh.kavan', 'Child Xantech 8 Zone Amp Controller', 'MP6ZA-child-25', [name: "child-${Zone11Name}", label: "${settings.Zone11Name}", zone: 25, isComponent: false])
        addChildDevice('suresh.kavan', 'Child Xantech 8 Zone Amp Controller', 'MP6ZA-child-26', [name: "child-${Zone12Name}", label: "${settings.Zone12Name}", zone: 26, isComponent: false])
    }
    if (settings.NumberAmps.toInteger() == 3) {
        if (logEnable) log.debug 'Parent: 3 amp ceating 3er set of childerens'
        addChildDevice('suresh.kavan', 'Child Xantech 8 Zone Amp Controller', 'MP6ZA-child-31', [name: "child-${Zone13Name}", label: "${settings.Zone13Name}", zone: 31, isComponent: false])
        addChildDevice('suresh.kavan', 'Child Xantech 8 Zone Amp Controller', 'MP6ZA-child-32', [name: "child-${Zone14Name}", label: "${settings.Zone14Name}", zone: 32, isComponent: false])
        addChildDevice('suresh.kavan', 'Child Xantech 8 Zone Amp Controller', 'MP6ZA-child-33', [name: "child-${Zone15Name}", label: "${settings.Zone15Name}", zone: 33, isComponent: false])
        addChildDevice('suresh.kavan', 'Child Xantech 8 Zone Amp Controller', 'MP6ZA-child-34', [name: "child-${Zone16Name}", label: "${settings.Zone16Name}", zone: 34, isComponent: false])
        addChildDevice('suresh.kavan', 'Child Xantech 8 Zone Amp Controller', 'MP6ZA-child-35', [name: "child-${Zone17Name}", label: "${settings.Zone17Name}", zone: 35, isComponent: false])
        addChildDevice('suresh.kavan', 'Child Xantech 8 Zone Amp Controller', 'MP6ZA-child-36', [name: "child-${Zone18Name}", label: "${settings.Zone18Name}", zone: 36, isComponent: false])
    }
    setChildzones()
}
/*
def createChildDevices() {
    log.debug "Parent createChildDevices"
    addChildDevice("suresh.kavan","Child Xantech 8 Zone Amp Controller", "MP6ZA-child-11", [name: "child-${Zone1Name}", label: "${settings.Zone1Name}", zone: 11, isComponent: false])
    addChildDevice("suresh.kavan","Child Xantech 8 Zone Amp Controller", "MP6ZA-child-12", [name: "child-${Zone2Name}", label: "${settings.Zone2Name}", zone: 12, isComponent: false])
    addChildDevice("suresh.kavan","Child Xantech 8 Zone Amp Controller", "MP6ZA-child-13", [name: "child-${Zone3Name}", label: "${settings.Zone3Name}", zone: 13, isComponent: false])
    addChildDevice("suresh.kavan","Child Xantech 8 Zone Amp Controller", "MP6ZA-child-14", [name: "child-${Zone4Name}", label: "${settings.Zone4Name}", zone: 14, isComponent: false])
    addChildDevice("suresh.kavan","Child Xantech 8 Zone Amp Controller", "MP6ZA-child-15", [name: "child-${Zone5Name}", label: "${settings.Zone5Name}", zone: 15, isComponent: false])
    addChildDevice("suresh.kavan","Child Xantech 8 Zone Amp Controller", "MP6ZA-child-16", [name: "child-${Zone6Name}", label: "${settings.Zone6Name}", zone: 16, isComponent: false])
    setChildzones ()
}*/

def deleteChildren() {
    if (logEnable) log.debug 'Parent deleteChildren'
    def children = getChildDevices()
    children.each { child->
        deleteChildDevice(child.deviceNetworkId)
    }
}
def CloseTelnet() {
    if (logEnable) log.debug 'Closing telnet'
    interfaces.rawSocket.close()
    telnetClose()
    unschedule()
}
def installed() {
    log.info('Parent Xantech 8 Zone Amp Controller: installed()')
    createChildDevices()
    initialize()
}
def updated() {
    log.info('Parent Xantech 8 Zone Amp Controller: updated()')
    initialize()
//recreateChildDevices()
}
def pollSchedule() {
    forcePoll()
}

def initialize() {
    log.info('Parent Xantech 8 Zone Amp Controller: initialize()')
    state.delay = 250
    state.messageQueue = []
    state.queueProcessing = 'inactive'

    for (int i = 1; i <= 6; i++) {
        state["zoneQuery${i}"] = ["?${i}PR+", "?${i}VO+", "?${i}MU+", "?${i}SS+"]
    }

    interfaces.rawSocket.close()
    //telnetClose()
    // Connect with basic settings
    interfaces.rawSocket.connect('192.168.1.204', 5000)
    //telnetConnect([termChars:[43]], settings.IP, settings.port as int, '', '')
    unschedule()
    switch (settings.PollSchedule) {
        case '1': runEvery1Minute(pollSchedule); log.info('pollSchedule 1 minute'); break;
        case '2': runEvery5Minutes(pollSchedule); log.info('pollSchedule 5 minute'); break;
        case '3': runEvery15Minutes(pollSchedule); log.info('pollSchedule 15 minute'); break;
        case '4': runEvery30Minutes(pollSchedule); log.info('pollSchedule 30 minute'); break;
        default: log.info('pollSchedule ERROR')
    }
    forcePoll()
}

def webSocketStatus(String status){
    if (logEnable) log.debug "webSocket ${status}"

    if ((status == "status: closing") && (state.wasExpectedClose)) {
        state.wasExpectedClose = false
        sendEvent(name: "Connection", value: "Closed")
        return
    } 
    else if(status == 'status: open') {
        log.info "websocket is open"
        // success! reset reconnect delay
        pauseExecution(1000)
        state.reconnectDelay = 1
        state.wasExpectedClose = false
        sendEvent(name: "Connection", value: "Open")
    } 
    else {
        log.warn "WebSocket error, reconnecting."
        sendEvent(name: "Connection", value: "Reconnecting")
        reconnectWebSocket()
    }
}


def pollAmp1 () {
    if (logEnable) log.debug 'Polling First 8 Zones'
    //sendMsg("?1PR+")
    sendMsg(state.zoneQuery1)
}
def pollAmp2 () {
    if (logEnable) log.debug 'Polling second 8 Zones'
    sendMsg('?2PR')
}
def pollAmp3 () {
    if (logEnable) log.debug 'Polling third 8 Zones'
    sendMsg('?3PR')
}

def forcePoll() {
    if (logEnable) 'forcePoll'
    runIn(1, 'pollAmp1')
    if (settings.NumberAmps.toInteger() > 1) {
        runIn(4, 'pollAmp2')
    }
    if (settings.NumberAmps.toInteger() == 3) {
        runIn(7, 'pollAmp3')
    }
/*    if (logEnable) log.debug "Polling"
    sendMsg("?10")*/
}
def poll() { forcePoll() }

def sendMsgOld(String msg) {
    if(logEnable) log.debug ('Sending telnet msg: ' + msg)
    interfaces.rawSocket.sendMessage(msg + '+')
    return new hubitat.device.HubAction(msg, hubitat.device.Protocol.TELNET)
}

// Modified sendMsg function with overloading
def sendMsg(String msg) {
    sendMessageInternal(msg) // Call the helper
}

def sendMsg(List messages) {
    messages.each { sendMessageInternal(it) } // Iterate the array
}

// Helper function to handle adding to queue
def sendMessageInternal(String msg) {
    if (logEnable) log.debug('Queueing telnet msg: ' + msg)
    state.messageQueue.add(msg)

    // Conditional triggering of queue processing
    if (logEnable) log.debug('sendMessageInternal: ' + state.queueProcessing + state.messageQueue.size())
    if (state.queueProcessing != 'active') {
        runInMillis(state.delay, 'processQueue')
    //processQueue(state.delay) // Zero delay for immediate processing in this case
    }
}

// Message Processing Function
def processQueue(int nextDelay = state.delay) {
    if (logEnable) log.debug('processQ: ' + state.queueProcessing)
    state.queueProcessing = 'active' // Track if currently sending messages
  //setState("queueProcessing", "active") // Track if currently sending messages

    def msg = state.messageQueue.pop() // Remove & get the next message

    if (msg) { // Check if there was a message
        if (logEnable) log.debug('Sending telnet msg: ' + msg)
        interfaces.rawSocket.sendMessage(msg)

        if (state.messageQueue.size() == 0) {  // Additional check for empty queue
            state.queueProcessing = 'inactive'
            log.debug('messageQueue was processed and now empty, queueProcessing set to inactive')
        } else {
            // Schedule processing of the next message
            log.debug('setting delayed execution')
            runInMillis(state.delay, 'processQueue')
        }
  } else {
        // Queue was empty
        state.queueProcessing = 'inactive' // Track if currently sending messages
    //setState("queueProcessing", "inactive")
    }
}

/* groovylint-disable-next-line UnusedPrivateMethod */
private parse(String msg) {
    // todo: IF "OK" or "ERROR" then throw away
    // todo: handle a msg that has multiple messages in it
    msg = new String(hubitat.helper.HexUtils.hexStringToByteArray(msg))
    msg = msg.trim()
    //if (logEnable) log.debug("Parse recive: " + msg + ": " + msg.substring(0,1) + " -- " + msg.substring(1,2))

    //msg = new String(hubitat.helper.HexUtils.hexStringToByteArray(hexStr)).trim()
    msg = msg.trim()

    if (logEnable) log.debug('Parse recive: ' + msg + ': ' + msg.substring(0, 1) + ' -- ' + msg.substring(1, 2))
    // ?1PR0
    //if (!(msg.contains("Command Error")) && (msg.length()>5) && (msg.startsWith("#>"))){
    //if (msg.substring(1,3)==("#>")) {
    if (msg.substring(0, 1) == ("?")) {
        def children = getChildDevices()
        children.each { child->
            //log.debug("child: " + child.currentValue("zone"))
            if (msg.substring(1, 2).toInteger() == child.currentValue('zone')) {
                //log.debug("found child:" + child.currentValue("zone"))
                child.UpdateData(msg)
                if(logEnable) log.debug ('found parsing match: ' + msg)
            }
        }
    }
        }
def telnetStatus(String status) {
    log.warn 'telnetStatus: error: ' + status
    if (status != 'receive error: Stream is closed') {
        log.error 'Connection was dropped.'
        initialize()
    }
}
def getChanelName (Number channel) {
    def channelName = null
    if (channel == 1) channelName = Channel1Name
    if (channel == 2) channelName = Channel2Name
    if (channel == 3) channelName = Channel3Name
    if (channel == 4) channelName = Channel4Name
    if (channel == 5) channelName = Channel5Name
    if (channel == 6) channelName = Channel6Name
    return channelName
}
