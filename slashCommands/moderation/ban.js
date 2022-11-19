const { EmbedBuilder, SlashCommandBuilder } = require("discord.js");
const { PermissionFlagsBits } = require("discord-api-types/v10");

module.exports = {
    data: new SlashCommandBuilder()
        .setName("ban")
        .setDescription("Ban specify user!")
        .addUserOption(option => option.setName("user").setDescription("The user").setRequired(true))
        .addStringOption(option => option.setName("reason").setDescription("The reason").setRequired(false))
        .setDefaultMemberPermissions(PermissionFlagsBits.BanMembers | PermissionFlagsBits.KickMembers)
        .setDMPermission(false),
    execute: async (interaction) => {
        const member = interaction.options.getMember("user");
        const reason = interaction.options.getString("reason") || "No reason provided";

        if (member.id === interaction.user.id) {
            return interaction.reply({ content: "You cannot ban yourself!", ephemeral: true });
        }

        if (member.bot) {
            return interaction.reply({ content: "You cannot ban a bot!", ephemeral: true });
        }

        if (!member.bannable) {
            return interaction.reply({ content: "You cannot ban this user!", ephemeral: true });
        }

        const embed = new EmbedBuilder()
            .setTitle(`🔨 ${member.user.tag} has been banned!`)
            .setThumbnail(member.user.displayAvatarURL({ dynamic: true, size: 4096 }))
            .setDescription(`**Reason:** ${reason}`)
            .setColor("#2f3136")
            .setTimestamp()
            .setFooter({ text: `Requested by ${interaction.user.tag}`, iconURL: interaction.user.displayAvatarURL({ dynamic: true, size: 4096 }) });

        member.send({ embeds: [embed] }).catch(() => {});
        interaction.reply({ embeds: [embed] });
        await member.ban({ reason: `${reason}` });
    }
};