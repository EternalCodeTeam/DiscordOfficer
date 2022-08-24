module.exports = async (interaction, client) => {
    const queue = eternalPlayer.getQueue(interaction.guild.id);
    const volume = interaction.options.getNumber('level');

    if(!queue || !queue.playing) {
        return interaction.reply({
            content: `Queue is empty`,
            ephemeral: true
        });
    }

    if(queue.volume === volume) {
        return interaction.reply({
            content: `Volume is this same as u provided | Level: ${queue.volume}`,
            ephemeral: true
        });
    }

    eternalPlayerVolume = volume;

    return interaction.reply({
        content: queue.setVolume(volume) ? `Volume set to **${volume}**%` : `Error with setting volume :cry:`,
        ephemeral: true
    })
}