package com.eternalcode.discordapp.leveling.games.task;

import com.eternalcode.discordapp.config.ConfigManager;
import com.eternalcode.discordapp.leveling.games.CodeImageGameData;
import com.eternalcode.discordapp.leveling.games.configuration.CodeGameConfiguration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.lang3.RandomStringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.TimerTask;

public class GenerateImageWithCode extends TimerTask {

    private final CodeImageGameData codeImageGameData;
    private final CodeGameConfiguration codeGameConfiguration;
    private final JDA jda;
    private final ConfigManager dataManager;

    public GenerateImageWithCode(CodeImageGameData codeImageGameData, CodeGameConfiguration codeGameConfiguration, JDA jda, ConfigManager dataManager) {
        this.codeImageGameData = codeImageGameData;
        this.codeGameConfiguration = codeGameConfiguration;
        this.jda = jda;
        this.dataManager = dataManager;
    }

    @Override
    public void run() {
        BufferedImage image = this.generateCodeImage();
        this.sendCodeImageMessage(image);
    }

    private BufferedImage generateCodeImage() {
        String code = RandomStringUtils.randomAlphanumeric(6);
        boolean isUsed = false;
        boolean gameActive = true;
        Instant lastUpdated = Instant.now();

        this.codeImageGameData.code = code;
        this.codeImageGameData.isUsed = isUsed;
        this.codeImageGameData.gameActive = gameActive;
        this.codeImageGameData.lastUpdated = lastUpdated;
        this.dataManager.save(this.codeImageGameData);

        BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        image.getGraphics().drawString(code, 50, 80);

        return image;
    }

    private void sendCodeImageMessage(BufferedImage image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
        }
        catch (IOException exception) {
            exception.printStackTrace();
            return;
        }

        TextChannel channel = this.jda.getTextChannelById(this.codeGameConfiguration.channelId);
        if (channel != null) {
            FileUpload fileUpload = FileUpload.fromData(baos.toByteArray(), "game.png");
            channel.sendMessage(this.codeGameConfiguration.codeText)
                .addFiles(fileUpload)
                .queue();
        }
    }

}
