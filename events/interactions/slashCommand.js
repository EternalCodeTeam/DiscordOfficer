const {EmbedBuilder, Collection, PermissionsBitField} = require('discord.js');
const ms = require('ms');
const cooldown = new Collection();

module.exports = async (client, interaction) => {
    const slashCommand = client.slashCommands.get(interaction.commandName);

    if (interaction.type === 4) {
        if (slashCommand.autocomplete) {
            const choices = [];
            await slashCommand.autocomplete(interaction, choices)
        }
    }
    if (interaction.type !== 2) return;
    if (!slashCommand) return client.slashCommands.delete(interaction.commandName);

    try {
        if (slashCommand.cooldown && cooldown.has(`slash-${slashCommand.name}${interaction.user.id}`))
            return interaction.reply({
                content: `Musisz jeszcze poczekać ${ms(cooldown.get(`slash-${slashCommand.name}${interaction.user.id}`) - Date.now(), {long: true})} zanim ponownie użyjesz tego polecenia!`
            });

        if (slashCommand.userPerms || slashCommand.botPerms) {
            if (!interaction.memberPermissions.has(PermissionsBitField.resolve(slashCommand.userPerms || []))) {
                return interaction.reply({
                    embeds: [
                        new EmbedBuilder()
                            .setDescription(`🚫 ${interaction.user}, Nie posiadasz permisji \`${slashCommand.userPerms}\` by użyć tego polecenia!`)
                            .setColor('Red')
                    ]
                })
            }
            if (!interaction.guild.members.cache.get(client.user.id).permissions.has(PermissionsBitField.resolve(slashCommand.botPerms || []))) {
                return interaction.reply({
                    embeds: [
                        new EmbedBuilder()
                            .setDescription(`🚫 ${interaction.user}, Nie posiadam uprawnienia \`${slashCommand.botPerms}\` by wykonać to polecenie!`)
                            .setColor('Red')
                    ]
                })
            }
        }

        await slashCommand.run(client, interaction);
        if (slashCommand.cooldown) {
            cooldown.set(`slash-${slashCommand.name}${interaction.user.id}`, Date.now() + slashCommand.cooldown)
            setTimeout(() => {
                cooldown.delete(`slash-${slashCommand.name}${interaction.user.id}`)
            }, slashCommand.cooldown)
        }
    } catch (error) {
        console.log(error);
    }
}