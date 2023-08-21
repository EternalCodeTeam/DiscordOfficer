package com.eternalcode.discordapp.leveling.games;

import com.eternalcode.discordapp.config.ConfigManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.lang3.RandomStringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.TimerTask;

public class GenerateImageWithCodeTask extends TimerTask {

    private final CodeImageGameData codeImageGameData;
    private final CodeGameConfiguration codeGameConfiguration;
    private final JDA jda;
    private final ConfigManager dataManager;

    public GenerateImageWithCodeTask(CodeImageGameData codeImageGameData, CodeGameConfiguration codeGameConfiguration, JDA jda, ConfigManager dataManager) {
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
        Graphics2D graphics = image.createGraphics();

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

        Font font = new Font("Arial", Font.BOLD, 24);
        graphics.setFont(font);
        graphics.setColor(Color.BLACK);

        FontMetrics fontMetrics = graphics.getFontMetrics();
        int x = (image.getWidth() - fontMetrics.stringWidth(code)) / 2;
        int y = (image.getHeight() - fontMetrics.getHeight()) / 2 + fontMetrics.getAscent();

        graphics.drawString(code, x, y);
        graphics.dispose();

        return image;
    }

    private void sendCodeImageMessage(BufferedImage image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, "png", byteArrayOutputStream);
        }
        catch (IOException exception) {
            exception.printStackTrace();
            return;
        }

        TextChannel channel = this.jda.getTextChannelById(this.codeGameConfiguration.channelId);
        if (channel != null) {
            FileUpload fileUpload = FileUpload.fromData(byteArrayOutputStream.toByteArray(), "game.png");
            channel.sendMessage(this.codeGameConfiguration.codeText)
                    .addFiles(fileUpload)
                    .queue();
        }
    }

}
