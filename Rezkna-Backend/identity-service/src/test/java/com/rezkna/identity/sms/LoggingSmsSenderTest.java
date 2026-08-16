package com.rezkna.identity.sms;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingSmsSenderTest {

    private final LoggingSmsSender smsSender = new LoggingSmsSender();
    private ListAppender<ILoggingEvent> appender;
    private Logger logger;

    @BeforeEach
    void attachAppender() {
        logger = (Logger) LoggerFactory.getLogger(LoggingSmsSender.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        logger.detachAppender(appender);
    }

    @Test
    void neverLogsTheMessageContentContainingTheOtpCode() {
        smsSender.send("21612345678", "Your Rezkna verification code is 042381. It expires in 10 minutes.");

        String logged = appender.list.stream().map(ILoggingEvent::getFormattedMessage).findFirst().orElse("");
        assertThat(logged).doesNotContain("042381");
    }

    @Test
    void masksAllButTheLastFourDigitsOfThePhoneNumber() {
        smsSender.send("21612345678", "irrelevant message content");

        String logged = appender.list.stream().map(ILoggingEvent::getFormattedMessage).findFirst().orElse("");
        assertThat(logged).contains("*******5678");
        assertThat(logged).doesNotContain("21612345678");
    }

    @Test
    void masksAShortPhoneNumberEntirely() {
        smsSender.send("123", "irrelevant");

        String logged = appender.list.stream().map(ILoggingEvent::getFormattedMessage).findFirst().orElse("");
        assertThat(logged).contains("****");
    }
}
