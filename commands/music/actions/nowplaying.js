const {EmbedBuilder} = require("discord.js");

module.exports = async (interaction, client) => {
    const queue = eternalPlayer.getQueue(interaction.guild.id);

    if(!queue || !queue.playing)
        return interaction.reply({
            content: `Your queue is empty`,
            ephemeral: true
        })

    return interaction.reply({
        embeds: [
            new EmbedBuilder()
                .setImage(queue.current.thumbnail)
                .setAuthor({ name: queue.current.title, iconURL: interaction.member.displayAvatarURL({ size: 1024, dynamic: true })})
                .setDescription(`Progress:
                \n ${queue.createProgressBar()} (**${queue.getPlayerTimestamp().current}**)
                \nRequested by: ${queue.current.requestedBy}
                \nVolume: **${queue.volume}%**`)
        ],
        ephemeral: true
    })
}