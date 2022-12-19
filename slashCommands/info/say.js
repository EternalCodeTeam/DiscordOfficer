const { SlashCommandBuilder } = require("@discordjs/builders");
const { PermissionFlagsBits, ChannelType } = require("discord-api-types/v10");

module.exports = {
    data: new SlashCommandBuilder()
        .setName("say")
        .setDescription("Make the bot say something")
        .setDefaultMemberPermissions(PermissionFlagsBits.ManageMessages)
        .addStringOption(option => option.setName("message").setDescription("The message").setRequired(true))
        .addChannelOption(option => option.setName("channel").setDescription("The channel").setRequired(false).addChannelTypes(ChannelType.GuildText)),
    execute: async (interaction) => {
        const message = interaction.options.getString("message");
        const channel = interaction.options.getChannel("channel") || interaction.channel;

        interaction.reply({ content: `Message sent in <#${channel.id}>`, ephemeral: true });
        await channel.send(message);
    }
}