package de.treona.shelfs.io.logger.layouts;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BasicLayout extends LayoutBase<ILoggingEvent> {

    @Override
    public String doLayout(ILoggingEvent event) {
        return "[" +
                this.getMessageTimeStamp() +
                "] [" +
                this.formatLevel(event.getLevel().toString()) +
                "] [" +
                event.getLoggerName() +
                "]: " +
                event.getFormattedMessage() +
                CoreConstants.LINE_SEPARATOR;
    }

    private String formatLevel(String level) {
        if (level.length() <= 1)
            return level;
        else
            return level.substring(0, 1) + level.substring(1, level.length()).toLowerCase();
    }

    private String getMessageTimeStamp() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        return simpleDateFormat.format(new Date());
    }
}
