const {EmbedBuilder} = require("discord.js");
module.exports = async (modal) => {
    if(modal.customId !== "kick-modal")
        return;

    let user = await modal.guild.members.fetch(modal.getTextInputValue('user-kicked'));
    if(user === null)
        return modal.reply({ content: `User not found, try again`, ephemeral: true })

    try {
        user.send({
            embeds: [
                new EmbedBuilder()
                    .setDescription(`🚫 **Kick** \n U are kicked from ${modal.guild.name} by ${modal.member.user.username} \nReason: ${modal.getTextInputValue('kick-reason') ?? 'No reason given'}`)
                    .setColor('Red')
            ]
        })
    } catch (error) {
        console.log(error)
    }
    await user.kick({reason: `${modal.getTextInputValue('kick-reason') ?? 'No reason given'}`})
    modal.reply({
        embeds: [
            new EmbedBuilder()
                .setDescription(`🚫 <@${modal.user.id}> kicked user \`${user.user.username}\`\nReason: ${modal.getTextInputValue('kick-reason') ?? 'No reason given'}`)
                .setColor('Red')
        ]
    });
}