package com.oop.orangeengine.main.logger;

import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.plugin.EnginePlugin;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class OLogger {
    private String error_prefix = "&c[OLogger ERROR]: &r";
    private String warn_prefix = "&4[OLogger WARN]: &r";
    private String normal_prefix = "&e[OLogger]: &r";
    private String debug_prefix = "&e[OLogger DEBUG]: &r";
    private String loggerName;

    @Setter
    private boolean debugMode = false;

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

    private void send(String message, Object ...args) {
        if (message.contains("{}") && args.length > 0) {
            int currentObjectIndex = 0;
            int currentCharIndex = 0;
            char[] messageArray = message.toCharArray();

            StringBuilder builder = new StringBuilder();

            while (currentCharIndex < message.length()) {
                char currentChar = messageArray[currentCharIndex];
                if (currentChar == '{' && messageArray.length > currentCharIndex + 1) {
                    char nextChar = messageArray[currentCharIndex + 1];
                    if (nextChar == '}') {
                        currentCharIndex += 2;
                        if (args.length > currentObjectIndex) {
                            builder.append(args[currentObjectIndex]);
                            currentObjectIndex += 1;
                        }
                        continue;
                    }
                }

                builder.append(currentChar);
                currentCharIndex++;
            }
            message = builder.toString();
        }

        Helper.print(message);
    }

    public void printEmpty() {
        send("");
    }

    public void print(Object object, Object ...args) {
        send(normal_prefix + object.toString(), args);
    }

    public void printError(Object object, Object ...args) {
        send(error_prefix + object.toString(), args);
    }

    public void printWarning(Object object, Object ...args) {
        send(warn_prefix + object.toString(), args);
    }

    public void printDebug(Object object, Object ...args) {
        if (debugMode)
            send(debug_prefix + object, args);
    }

    public void error(Throwable exception) {
        printError(exception.getClass().getSimpleName() + " was caught in " + getLoggerName() + ": " + exception.getMessage());
        for (StackTraceElement ste : exception.getStackTrace()) {
            send("&c - " + ste.toString());
        }

        send("");
        if (exception.getCause() != null) {
            send("&4Caused By " + exception.getCause().getMessage());
            for (StackTraceElement ste : exception.getCause().getStackTrace()) {
                send("&c - " + ste.toString());
            }
        }
    }

    public void error(Throwable exception, String message) {
        printError("Exception was caught in " + getLoggerName() + ": " + message);
        for (StackTraceElement ste : exception.getStackTrace()) {
            send("&c - " + ste.toString());
        }

        send("");

    }

    public void throwError(String message) {
        throw new IllegalStateException(message);
    }
}
