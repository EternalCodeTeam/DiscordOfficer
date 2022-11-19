const { EmbedBuilder, SlashCommandBuilder } = require("discord.js");
const { PermissionFlagsBits } = require("discord-api-types/v10");

module.exports = {
    data: new SlashCommandBuilder()
        .setName("kick")
        .setDescription("kick specify user!")
        .addUserOption(option => option.setName("user").setDescription("The user").setRequired(true))
        .addStringOption(option => option.setName("reason").setDescription("The reason").setRequired(false))
        .setDefaultMemberPermissions(PermissionFlagsBits.KickMembers)
        .setDMPermission(false),
    execute: async (interaction) => {
        const member = interaction.options.getMember("user");
        const reason = interaction.options.getString("reason") || "No reason provided";

        if (member.id === interaction.user.id) {
            return interaction.reply({ content: "You cannot kick yourself!", ephemeral: true });
        }

        if (member.bot) {
            return interaction.reply({ content: "You cannot kick a bot!", ephemeral: true });
        }

        if (!member.kickable) {
            return interaction.reply({ content: "You cannot kick this user!", ephemeral: true });
        }

        const embed = new EmbedBuilder()
            .setTitle(`🔨 ${member.user.tag} has been kicked!`)
            .setThumbnail(member.user.displayAvatarURL({ dynamic: true, size: 4096 }))
            .setDescription(`**Reason:** ${reason}`)
            .setColor("#2f3136")
            .setTimestamp()
            .setFooter({ text: `Requested by ${interaction.user.tag}`, iconURL: interaction.user.displayAvatarURL({ dynamic: true, size: 4096 }) });

        member.send({ embeds: [embed] }).catch(() => {});
        interaction.reply({ embeds: [embed] });
        await member.kick(reason);
    }
};