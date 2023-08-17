package com.muhammaddaffa.nextgens.events.managers;

import com.muhammaddaffa.nextgens.events.Event;
import com.muhammaddaffa.nextgens.utils.Common;
import com.muhammaddaffa.nextgens.utils.Config;
import com.muhammaddaffa.nextgens.utils.Executor;
import com.muhammaddaffa.nextgens.utils.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class EventManager {

    private final List<Event> eventList = new ArrayList<>();

    private Event activeEvent;
    private int index;
    private Double waitTime;

    public List<Event> getEvents() {
        return new ArrayList<>(this.eventList);
    }

    public List<String> getEventName() {
        List<String> list = this.eventList.stream().map(Event::getId).collect(Collectors.toList());
        list.add("random");
        return list;
    }

    @Nullable
    public Event getEvent(String id) {
        return this.eventList.stream()
                .filter(event -> event.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @NotNull
    public Event getRandomEvent() {
        Event event = this.eventList.get(ThreadLocalRandom.current().nextInt(this.eventList.size()));
        if (this.activeEvent != null) {
            while (event.getId().equals(this.activeEvent.getId())) {
                event = this.eventList.get(ThreadLocalRandom.current().nextInt(this.eventList.size()));
            }
        }
        return event;
    }

    @NotNull
    public Event getNextEvent(boolean count) {
        int next = this.index + 1;
        // check if the next event is existed or not
        if (!Common.isValid(this.eventList, next)) {
            // assign the index to the first one
            if (count) {
                this.index = 0;
            }
            // if there is no next event
            // return the first event
            return this.eventList.get(0);
        }
        // assign the index to the next event
        if (count) {
            this.index = next;
        }
        // if the next event is present, return the next event
        return this.eventList.get(next);
    }

    public void loadEvents() {
        // clear the event list first
        this.eventList.clear();
        // get all variables we want
        FileConfiguration config = Config.EVENTS.getConfig();
        // check if there are any events or not
        if (!config.isConfigurationSection("events.events")) {
            return;
        }
        // loop through all events
        for (String id : config.getConfigurationSection("events.events").getKeys(false)) {
            // create the event object
            Event event = Event.createEvent(config, "events.events." + id, id);
            // if the event is not valid, skip it
            if (event == null) {
                Logger.warning("Failed to load event '"+ id + "' because the configuration is invalid!");
                continue;
            }
            // cache it
            this.eventList.add(event);
        }
        // log message
        Logger.info("Successfully loaded " + this.eventList.size() + " events!");
    }

    public void startTask() {
        // set the default wait time
        if (this.waitTime == null) {
            this.waitTime = this.getDefaultWaitTime();
        }

        Executor.asyncTimer(20L, 2L, () -> {
            // if the event is not enabled, don't bother it
            if (!this.isEnabled()) {
                return;
            }
            this.whenEventIsOnCooldown();
            this.whenEventIsRunning();
        });
    }

    private void whenEventIsOnCooldown() {
        // if there is an event running
        if (this.activeEvent != null) {
            this.waitTime = this.getDefaultWaitTime();
            return;
        }
        // below or equals to 0 wait time
        if (this.waitTime <= 0) {
            // assign next event
            if (this.isRandom()) {
                this.activeEvent = this.getRandomEvent().clone();
            } else {
                this.activeEvent = this.getNextEvent(true).clone();
            }
            // send start messages
            this.activeEvent.getStartMessage().forEach(Common::broadcast);
            // reset back the wait time
            this.waitTime = this.getDefaultWaitTime();
            // return the code
            return;
        }
        // decrease the wait time
        this.waitTime -= 0.1;
    }

    private void whenEventIsRunning() {
        // if there is no event running, skip it
        if (this.getActiveEvent() == null) {
            return;
        }
        // if the start time reaches 0
        if (this.activeEvent.getDuration() <= 0) {
            // send end message
            this.activeEvent.getEndMessage().forEach(Common::broadcast);
            this.activeEvent = null;
            return;
        }
        // reduce the duration
        this.activeEvent.setDuration(this.activeEvent.getDuration() - 0.1);
    }

    public void forceStart(Event event) {
        // assign the active event
        this.activeEvent = event.clone();
        // send start messages
        this.activeEvent.getStartMessage().forEach(Common::broadcast);
        // reset back the wait time
        this.waitTime = this.getDefaultWaitTime();
    }

    public boolean forceEnd() {
        if (this.activeEvent == null) {
            return false;
        }
        this.activeEvent.getEndMessage().forEach(Common::broadcast);
        this.activeEvent = null;
        this.waitTime = this.getDefaultWaitTime();
        return true;
    }

    public void load() {
        // get the config
        FileConfiguration config = Config.DATA.getConfig();
        // get the saved data
        String eventId = config.getString("events.id");
        if (eventId != null && this.getEvent(eventId) != null) {
            Event event = this.getEvent(eventId);
            event.setDuration(config.getDouble("events.timer"));
            // set the active event to this one
            this.activeEvent = event;
        }
        int waitTime = config.getInt("event-wait-time");
        int index = config.getInt("event-index");
        // assign the variables
        this.waitTime = config.get("event-wait-time") == null ? this.getDefaultWaitTime() : waitTime;
        this.index = config.get("event-index") == null ? -1 : index;
    }
    public void save() {
        // get variables
        Config data = Config.DATA;
        FileConfiguration config = data.getConfig();
        // save it
        if (this.activeEvent == null) {
            config.set("events", null);
        } else {
            config.set("events.id", this.activeEvent.getId());
            config.set("events.timer", this.activeEvent.getDuration());
        }
        config.set("event-wait-time", this.waitTime);
        config.set("event-index", this.index);
        // finally, save the config
        data.saveConfig();
    }

    public Event getActiveEvent() {
        return activeEvent;
    }

    public boolean isEnabled() {
        return Config.EVENTS.getBoolean("events.enabled");
    }

    public boolean isRandom() {
        return Config.EVENTS.getBoolean("events.random");
    }

    public double getWaitTime() {
        return this.waitTime;
    }

    public double getDefaultWaitTime() {
        return Config.EVENTS.getDouble("events.wait-time");
    }

}
