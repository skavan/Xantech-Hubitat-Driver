/*
 *  Pico color temp bulb controller instance
 *  Button 1 presses:
 *  1: turn bulb(s) on (can have the bulb programmed to set color temp depending on time of day, when it is turned on)
 *  2: set bulb(s) to Nightlight 
 *  3: set bulb(s) to Relax
 *  4: set bulb(s) to Read
 *  5: set bulb(s) to Concentrate
 *  6: set bulb(s) to Energize
 *  7: back to Nightlight, etc
 */
definition(name: "Pico Color Temp Bulb Controller Instance",
           namespace: "hubitat",
           author: "dagrider",
           description: "Cycle through bulb color temps with button 1, off with button 5, dim with buttons 2 and 4 (instance)",
           category: "Green Living",
           parent: "hubitat:Pico Color Temp Bulb Controller",
           iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
           iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png")

preferences {
    section {
        input "thePico", "capability.pushableButton", title: "Choose Pico Remote"
        input "theBulb", "capability.colorTemperature", title: "Choose bulb/group to control"
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
    log.debug "initialize"
    subscribe(thePico, "pushed", pushedHandler)
    subscribe(theBulb, "switch", switchHandler)
    state.pressNum = 0
}

def pushedHandler(evt) {
    switch(evt.value) { 
        case "1": 
            log.debug "button 1 pushed"
        
            if (state.pressNum == 0) {
                    log.debug "pressNum is 0, On"
                    bulbOn()
                    sendEvent(name: "on", value: "pressNum: $state.pressNum", descriptionText: "Turn bulb on")
                    state.pressNum = 1
            } else if (state.pressNum == 1) {
                    log.debug "pressNum is 1, Nightlight"
                    theBulb.setLevel(1)
                    theBulb.setColorTemperature(2237)
                    sendEvent(name: "on", value: "pressNum: $state.pressNum", descriptionText: "Set bulb to Nightlight")
                    state.pressNum = 2
            } else if (state.pressNum == 2) {
                    log.debug "pressNum is 2, Relax"
                    theBulb.setColorTemperature(2237)
                    theBulb.setLevel(57)
                    sendEvent(name: "on", value: "pressNum: $state.pressNum", descriptionText: "Set bulb to Relax")
                    state.pressNum = 3
            } else if (state.pressNum == 3) {
                    log.debug "pressNum is 3, Read"
                    theBulb.setColorTemperature(2890)
                    theBulb.setLevel(100)
                    sendEvent(name: "on", value: "pressNum: $state.pressNum", descriptionText: "Set bulb to Read")
                    state.pressNum = 4
            } else if (state.pressNum == 4) {
                    log.debug "pressNum is 4, Concentrate"
                    theBulb.setColorTemperature(4292)
                    theBulb.setLevel(100)
                    sendEvent(name: "on", value: "pressNum: $state.pressNum", descriptionText: "Set bulb to Concentrate")
                    state.pressNum = 5
            } else if (state.pressNum == 5) {
                    log.debug "pressNum is 5, Energize"
                    theBulb.setColorTemperature(6536)
                    theBulb.setLevel(100)
                    sendEvent(name: "on", value: "pressNum: $state.pressNum", descriptionText: "Set bulb to Energize")
                    state.pressNum = 1
            }
        
            break;
        case "2": 
            log.debug "button 2 pushed"
        
            if (theBulb.currentValue("switch") == "off") {
                bulbOn()
                sendEvent(name: "brighten", value: "", descriptionText: "Brighten bulb to on")
            } else {
                def lvl = getLevel(1)
                log.debug "brighten to level $lvl"
                theBulb.setLevel(lvl)
                sendEvent(name: "brighten", value: "level: $lvl", descriptionText: "Brighten bulb to $lvl")
            }
        
            break;
        case "3": 
            // todo: center button
            break;
        case "4": 
            log.debug "button 4 pushed"
            
            if (theBulb.currentValue("switch") != "off") {
                def lvl = getLevel(0)
                log.debug "dim to level $lvl"
                
                if (lvl == 0) {
                    bulbOff()
                    sendEvent(name: "dim", value: "", descriptionText: "Dim bulb to off")
                } else {
                    theBulb.setLevel(lvl)
                    sendEvent(name: "dim", value: "level: $lvl", descriptionText: "Dim bulb to $lvl")
                }
            } 
        
            break;
        case "5": 
            log.debug "button 5 pushed"
            bulbOff()
            sendEvent(name: "off", value: "pressNum: $state.pressNum", descriptionText: "Turn bulb off")
            break;
        default:
            return
    }
}

private bulbOn() {
    theBulb.on()
    theBulb.refresh()
}

private bulbOff() {
    theBulb.off()
    theBulb.refresh()
    state.pressNum = 0
}

def switchHandler(evt) {
    switch(evt.value) { 
        case "on": 
            state.pressNum = 1
            break
        case "off":
            state.pressNum = 0
            break
    }
}

private int getLevel(typ) {
    def lvl = theBulb.currentValue("level").toInteger()
    log.debug "current level: $lvl"
    
    switch (lvl) {
        case 0..10: 
            lvl = 0
            break
        case 11..30:
            lvl = 20
            break
        case 31..50:
            lvl = 40
            break
        case 51..70:
            lvl = 60
            break
        case 71..90:
            lvl = 80
            break
        default:
            lvl = 100
            break
    }
    
    log.debug "adjusted level: $lvl"
    
    if (typ == 1) lvl = lvl + 20
    else lvl = lvl - 20
        
    if (lvl > 100) lvl = 100
    else if (lvl <= 0) lvl = 0
    
    log.debug "new level: $lvl"
    return lvl
}