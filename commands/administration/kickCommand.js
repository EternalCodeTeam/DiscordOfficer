const { ApplicationCommandType, GuildMember, PermissionsBitField, EmbedBuilder} = require('discord.js');
const { Modal, TextInputComponent, showModal} = require('discord-modals');
module.exports = {
    name: 'kick',
    description: "Kick user from server",
    default_member_permissions: "KickMembers",
    type: ApplicationCommandType.ChatInput,
    options: [
        {
            name: 'user',
            description: 'Select the user who got ban',
            type: 6,
            required: true,
        }
    ],
    run: async (client, interaction) => {
        if(interaction.options.get('user').member.permissions.has(PermissionsBitField.Flags.Administrator) || interaction.options.get('user').member.permissions.has(PermissionsBitField.Flags.KickMembers))
            return interaction.reply({ ephemeral: true, embeds: [
                    new EmbedBuilder()
                        .setDescription(`🚫 ${interaction.user}, U can't kick \`${interaction.options.get('user').user.username}\`!`)
                        .setColor('Red')
                ]})

        const kickModal = new Modal()
            .setCustomId('kick-modal')
            .setTitle('Kick member from this server')
            .addComponents(
                new TextInputComponent()
                    .setCustomId('user-kicked')
                    .setStyle('SHORT')
                    .setLabel("Don't modify this input!")
                    .setRequired(true)
                    .setDefaultValue(interaction.options.get('user').user.id),
                new TextInputComponent()
                    .setCustomId('kick-reason')
                    .setLabel('Reason')
                    .setStyle('LONG')
                    .setRequired(false)
                    .setPlaceholder('Write here reason of the kick')
            )

        await showModal(kickModal, {client, interaction});
    }
};