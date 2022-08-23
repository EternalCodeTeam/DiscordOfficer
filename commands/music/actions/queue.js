const { EmbedBuilder } = require("discord.js");
module.exports = async (interaction, client) => {
    const queue = eternalPlayer.getQueue(interaction.guild.id);

    if (!queue || !queue.playing)
        return interaction.reply({
            content: `Queue is empty`,
            ephemeral: true
        });

    const embed = new EmbedBuilder();
    embed.setColor("#FFFFFF");
    embed.setThumbnail(interaction.guild.iconURL({ size: 2048, dynamic: true }));

    embed.setDescription(
        `**Now playing**: ${queue.current.title}
        \n\n
        ${queue.tracks.map((track, i) => `**#${i++}** - ${track.title} | ${track.author} (Requested by: ${track.requestedBy})`).slice(0, 15).join("\n")}
        \n${queue.tracks.length > 15 ? `And ${queue.tracks.length - 15} songs` : ""}`);

    return interaction.reply({
        embeds: [embed],
        ephemeral: true
    });

};