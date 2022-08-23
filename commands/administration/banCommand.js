const { ApplicationCommandType, GuildMember, PermissionsBitField, EmbedBuilder} = require('discord.js');
const { Modal, TextInputComponent, showModal} = require('discord-modals');
module.exports = {
    name: 'ban',
    description: "Use ban hammer on user",
    default_member_permissions: "BanMembers",
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
        if(interaction.options.get('user').member.permissions.has(PermissionsBitField.Flags.Administrator) || interaction.options.get('user').member.permissions.has(PermissionsBitField.Flags.BanMembers))
            return interaction.reply({ ephemeral: true, embeds: [
                    new EmbedBuilder()
                        .setDescription(`🚫 ${interaction.user}, U can't ban \`${interaction.options.get('user').user.username}\`!`)
                        .setColor('Red')
                ]})

        const banModal = new Modal()
            .setCustomId('ban-modal')
            .setTitle('Ban member from this server')
            .addComponents(
                new TextInputComponent()
                    .setCustomId('user-banned')
                    .setStyle('SHORT')
                    .setLabel("Don't modify this input!")
                    .setRequired(true)
                    .setDefaultValue(interaction.options.get('user').user.id),
                new TextInputComponent()
                    .setCustomId('ban-reason')
                    .setLabel('Reason')
                    .setStyle('LONG')
                    .setRequired(false)
                    .setPlaceholder('Write here reason of the ban')
            )

        await showModal(banModal, {client, interaction});
    }
};