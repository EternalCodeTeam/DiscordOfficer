const { SlashCommandBuilder } = require("@discordjs/builders");
const { EmbedBuilder } = require("discord.js");
const os = require("os");

module.exports = {
    data: new SlashCommandBuilder()
        .setName("botinfo")
        .setDescription("Display bot's information"),
    execute: async (interaction) => {
        const embed = new EmbedBuilder()
            .setTitle(`🤖 ${interaction.client.user.tag} Bot's Information`)
            .setColor("#2f3136")
            .addFields(
                { name: "📊 Uptime", value: `<t:${Math.floor(interaction.client.readyTimestamp / 1000)}:R>`, inline: true },
                { name: "📡 Servers", value: `${interaction.client.guilds.cache.size}`, inline: true },
                { name: "📊 Memory Usage", value: `${(process.memoryUsage().heapUsed / 1024 / 1024).toFixed(2)} MB`, inline: true },
                { name: "📊 CPU Usage", value: `${Math.round(os.loadavg()[0] * 100) / 100}%`, inline: true }
            )
            .setThumbnail(interaction.client.user.displayAvatarURL({ dynamic: true, size: 4096 }))
            .setTimestamp()
            .setFooter({ text: `Requested by ${interaction.user.tag}`, iconURL: interaction.user.displayAvatarURL({ dynamic: true, size: 4096 }) })

        interaction.reply({ embeds: [embed] });
    }
};