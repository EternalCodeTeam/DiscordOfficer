const { SlashCommandBuilder } = require("@discordjs/builders");
const { EmbedBuilder } = require("discord.js");

module.exports = {
    data: new SlashCommandBuilder()
        .setName("server")
        .setDescription("Replies with server info!"),
    execute: async (interaction) => {
        const owner = await interaction.guild.fetchOwner();
        const totalMembers = await interaction.guild.members.cache.size;
        const onlineMembers = interaction.guild.members.cache.filter(member => member.presence.status !== "offline").size;

        const embed = new EmbedBuilder()
            .setTitle(`📊 ${interaction.guild.name}`)
            .setThumbnail(interaction.guild.iconURL({ dynamic: true, size: 4096 }))
            .addFields(
                { name: "👑 Owner", value: `${owner}`, inline: true },
                { name: "👥 Members", value: `${totalMembers}`, inline: true },
                { name: "🟢 Online Members", value: `${onlineMembers}`, inline: true },
                { name: "📅 Created At", value: `<t:${Math.floor(interaction.guild.createdTimestamp / 1000)}:F>`, inline: true },
                { name: "📊 Roles", value: `${interaction.guild.roles.cache.size}`, inline: true },
                { name: "📊 Channels", value: `${interaction.guild.channels.cache.size}`, inline: true }

            )
            .setColor("#2f3136")
            .setTimestamp()
            .setFooter({ text: `Requested by ${interaction.user.tag}`, iconURL: interaction.user.displayAvatarURL({ dynamic: true, size: 4096 }) });

        return interaction.reply({ embeds: [embed] });
    }
}