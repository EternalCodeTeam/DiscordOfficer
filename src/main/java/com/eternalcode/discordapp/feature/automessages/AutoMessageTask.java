package com.eternalcode.discordapp.feature.automessages;

import com.eternalcode.discordapp.feature.automessages.AutoMessageService.AutoMessageResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoMessageTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoMessageTask.class);

    private final AutoMessageService autoMessageService;

    public AutoMessageTask(AutoMessageService autoMessageService) {
        this.autoMessageService = autoMessageService;
    }

    @Override
    public void run() {
        try {
            LOGGER.debug("Starting auto message task...");

            AutoMessageResults results = autoMessageService.sendAutoMessages().join();

            if (results.failed() > 0) {
                LOGGER.warn(
                    "Auto message task completed with some failures: {}/{} successful",
                    results.successful(), results.total());
            }
            else {
                LOGGER.info("Auto message task completed successfully: {} messages sent", results.successful());
            }
        }
        catch (Exception exception) {
            LOGGER.error("Auto message task failed", exception);
        }
    }
}

