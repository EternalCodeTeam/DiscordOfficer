const { Events, EmbedBuilder } = require("discord.js");

module.exports = {
    name: Events.GuildMemberUpdate,
    once: false,
    async execute(oldMember, newMember) {
        if (oldMember.premiumSince !== newMember.premiumSince) {
            const embed = new EmbedBuilder()
                .setTitle("🎉 Boosted!")
                .setDescription(`**${newMember.user.tag}** has boosted the server!`)
                .setColor("#2f3136")
                .setTimestamp()

            newMember.guild.channels.cache.get(process.env.ETERNAL_BOOST_MESSAGE_CHANNEL_ID).send({ embeds: [embed] });
        }
    }
}