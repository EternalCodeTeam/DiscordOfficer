package com.eternalcode.discordapp.automessages;

public class AutoMessageTask implements Runnable {

    private final AutoMessageService autoMessageService;

    public AutoMessageTask(AutoMessageService autoMessageService) {
        this.autoMessageService = autoMessageService;
    }

    @Override
    public void run() {
        this.autoMessageService.sendAutoMessages();
    }
} 