const { SlashCommandBuilder } = require("@discordjs/builders");
const { ChannelType, PermissionFlagsBits } = require("discord-api-types/v10");

module.exports = {
    data: new SlashCommandBuilder()
        .setName("cooldown")
        .setDescription("Set cooldown for specified channel")
        .addChannelOption(option => option.setName("channel").setDescription("The channel").addChannelTypes(ChannelType.GuildText).setRequired(true))
        .addIntegerOption(option => option.setName("seconds").setDescription("The amount of seconds").setRequired(true).setMaxValue(21600).setMinValue(1))
        .setDefaultMemberPermissions(PermissionFlagsBits.ManageChannels),
    execute: async (interaction) => {
        const channel = interaction.options.getChannel("channel");
        const seconds = interaction.options.getInteger("seconds");

        await channel.edit({ rateLimitPerUser: seconds });
        interaction.reply({ content: `Successfully set cooldown for ${channel.toString()} to ${seconds} seconds!`, ephemeral: true });
    }
}