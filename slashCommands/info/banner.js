const { getBannerUrl } = require("../../utils/UserUtil");
const { SlashCommandBuilder } = require("@discordjs/builders");
const { EmbedBuilder } = require("discord.js");

module.exports = {
    data: new SlashCommandBuilder()
        .setName("banner")
        .setDescription("Display user's banner")
        .addUserOption(option => option.setName("user").setDescription("The user").setRequired(false)),
    execute: async (interaction) => {
        const user = interaction.options.getUser("user") || interaction.user;
        const image = await getBannerUrl(user.id, { size: 4096, dynamic: true });

        const embed = new EmbedBuilder()
            .setTitle(`🖼️ ${user.tag}'s banner`)
            .setImage(image)
            .setColor("#2f3136")
            .setFooter({ text: `Requested by ${interaction.user.tag}`, iconURL: interaction.user.displayAvatarURL({ dynamic: true, size: 4096 }) })
            .setTimestamp();

        return interaction.reply({ embeds: [embed] });
    }
};