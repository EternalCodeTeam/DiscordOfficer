const { ApplicationCommandType } = require('discord.js');
module.exports = {
    name: 'example1',
    description: "Example command for Officier",
    options: [
        {
            name: 'example-argument',
            description: 'Add role to a user.',
            type: 3,
            required: false,
        }
    ],
    type: ApplicationCommandType.ChatInput,
    run: async (client, interaction) => {
        return interaction.reply({ content: "Example command" })
    }
};