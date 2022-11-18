const { EmbedBuilder } = require("discord.js");
const { SlashCommandBuilder } = require("@discordjs/builders");

module.exports = {
    data: new SlashCommandBuilder()
        .setName("ping")
        .setDescription("Replies with Pong!"),
    execute: async (interaction) => {
        const embed = new EmbedBuilder()
            .setTitle("Pong! 🏓")
            .setDescription(`API Latency is **${Math.round(interaction.client.ws.ping)}ms**`)
            .setColor("#2f3136")
            .setTimestamp()
            .setFooter({ text: `Requested by ${interaction.user.tag}`, iconURL: interaction.user.displayAvatarURL({ dynamic: true, size: 4096 }) });
        interaction.reply({ embeds: [embed] });
    }
};