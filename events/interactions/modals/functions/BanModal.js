const {EmbedBuilder} = require("discord.js");
module.exports = async (modal, client) => {
    if(modal.customId !== "ban-modal")
        return;

    let user = await modal.guild.members.fetch(modal.getTextInputValue('user-banned'));
    await user.ban({reason: `${modal.getTextInputValue('ban-reason') ?? 'No reason given'}`})
    modal.reply({
        embeds: [
            new EmbedBuilder()
                .setDescription(`🚫 <@${modal.user.id}> banned user \`${user.user.username}\`\nReason: ${modal.getTextInputValue('ban-reason') ?? 'No reason given'}`)
                .setColor('Red')
        ]
    });
}