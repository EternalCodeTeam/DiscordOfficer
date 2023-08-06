package com.eternalcode.discordapp.games.task;

import com.eternalcode.discordapp.config.ConfigManager;
import com.eternalcode.discordapp.games.CodeImageGameData;
import com.eternalcode.discordapp.games.configuration.CodeGameConfiguration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.lang3.RandomStringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
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
        this.codeImageGameData.code = RandomStringUtils.randomAlphanumeric(6);
        this.codeImageGameData.isUsed = false;
        this.codeImageGameData.gameActive = true;
        this.codeImageGameData.lastUpdated = Instant.now();
        this.dataManager.save(this.codeImageGameData);

        BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        image.getGraphics().drawString(this.codeImageGameData.code, 120, 128);


        File file = new File("data" + File.separator + "game.png");
        try {
            ImageIO.write(image, "png", file);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        FileUpload fileUpload = FileUpload.fromData(file, "game.png");

        this.jda.getTextChannelById(this.codeGameConfiguration.channelId)
                .sendMessage(this.codeGameConfiguration.codeText)
                .addFiles(fileUpload)
                .queue();

    }
}
