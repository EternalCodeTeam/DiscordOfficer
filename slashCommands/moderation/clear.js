const { SlashCommandBuilder } = require("@discordjs/builders");
const { PermissionFlagsBits } = require("discord-api-types/v10");

module.exports = {
    data: new SlashCommandBuilder()
        .setName("clear")
        .setDescription("Clear specified amount of messages")
        .addIntegerOption(option => option.setName("amount").setDescription("The amount of messages to clear").setRequired(true).setMaxValue(100).setMinValue(1))
        .setDefaultMemberPermissions(PermissionFlagsBits.ManageMessages),
    execute: async (interaction) => {
        const amount = interaction.options.getInteger("amount");

        await interaction.channel.bulkDelete(amount, true).catch(() => {});
        interaction.reply({ content: `Successfully deleted ${amount} messages!`, ephemeral: true });
    }
}