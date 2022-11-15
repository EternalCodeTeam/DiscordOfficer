const { ApplicationCommandType, EmbedBuilder } = require("discord.js");

module.exports = {
    name: "ping",
    description: "Check bot's ping.",
    type: ApplicationCommandType.ChatInput,
    cooldown: 3000,
    run: async (client, interaction) => {
        const embed = new EmbedBuilder()
            .setTitle("Pong! 🏓")
            .setDescription(`API Latency is **${Math.round(client.ws.ping)}ms**`)
            .setColor("#2f3136");
        interaction.reply({ embeds: [embed] });
    }
};