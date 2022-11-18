const { EmbedBuilder, SlashCommandBuilder } = require("discord.js");

module.exports = {
    data: new SlashCommandBuilder()
        .setName("avatar")
        .setDescription("Display user's avatar")
        .addUserOption(option => option.setName("user").setDescription("The user").setRequired(false)),
    execute: async (interaction) => {
        const user = interaction.options.getUser("user") || interaction.user;

        const embed = new EmbedBuilder()
            .setTitle(`🖼️ ${user.tag}'s avatar`)
            .setImage(user.displayAvatarURL({ dynamic: true, size: 4096 }))
            .setColor("#2f3136")
            .setFooter({ text: `Requested by ${interaction.user.tag}`, iconURL: interaction.user.displayAvatarURL({ dynamic: true, size: 4096 }) })
            .setTimestamp();

        return interaction.reply({ embeds: [embed] });
    }
};