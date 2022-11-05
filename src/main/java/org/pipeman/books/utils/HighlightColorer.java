package org.pipeman.books.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ANSIConstants;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

public class HighlightColorer extends ForegroundCompositeConverterBase<ILoggingEvent> {

    @Override
    protected String getForegroundColorCode(ILoggingEvent event) {
        Level level = event.getLevel();
        return switch (level.toInt()) {
            case Level.ERROR_INT -> ANSIConstants.BOLD + ANSIConstants.RED_FG; // same as default color scheme
            case Level.WARN_INT -> ANSIConstants.BOLD + ANSIConstants.YELLOW_FG;// same as default color scheme
//            case Level.INFO_INT:
//                return ANSIConstants.DEFAULT_FG; // use CYAN instead of BLUE
            default -> ANSIConstants.DEFAULT_FG;
        };
    }
}
