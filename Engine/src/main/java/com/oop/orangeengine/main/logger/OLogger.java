package com.oop.orangeengine.main.logger;

import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.plugin.EnginePlugin;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;

@Getter
public class OLogger {
    private String error_prefix = "&c[OLogger ERROR]: &r";
    private String warn_prefix = "&4[OLogger WARN]: &r";
    private String normal_prefix = "&e[OLogger]: &r";
    private String debug_prefix = "&e[OLogger DEBUG]: &r";
    private String loggerName;

    public OLogger(EnginePlugin plugin) {
        name(plugin);
    }

    public OLogger name(String name) {
        this.loggerName = name;
        this.error_prefix = StringUtils.replace(error_prefix, "OLogger", loggerName);
        this.warn_prefix = StringUtils.replace(warn_prefix, "OLogger", loggerName);
        this.normal_prefix = StringUtils.replace(normal_prefix, "OLogger", loggerName);
        this.debug_prefix = StringUtils.replace(debug_prefix, "OLogger", loggerName);
        return this;
    }

    public OLogger name(EnginePlugin plugin) {
        return name(plugin.getName());
    }

    private void send(String message) {
        Helper.print(message);
    }

    public void printEmpty() {
        send("");
    }

    public void print(Object object) {
        send(normal_prefix + object.toString());
    }

    public void printError(Object object) {
        send(error_prefix + object.toString());
    }

    public void printWarning(Object object) {
        send(warn_prefix + object.toString());
    }

    public void printDebug(Object object) {
        send(debug_prefix + object.toString());
    }

    public void error(Throwable exception) {

        printError("Exception was caught in " + getLoggerName() + ": " + exception.getMessage());
        for (StackTraceElement ste : exception.getStackTrace()) {
            send("&c - " + ste.toString());
        }

        send("");

    }

    public void throwError(String message) {
        throw new IllegalStateException(message);
    }
}
