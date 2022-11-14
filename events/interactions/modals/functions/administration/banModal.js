const { EmbedBuilder } = require("discord.js");
module.exports = async (modal, client) => {
    if (modal.customId === "ban-modal") {
        let user = await modal.guild.members.fetch(modal.getTextInputValue("user-banned"));
        if (user === null)
            return modal.reply({ content: `User not found, try again`, ephemeral: true });

        await user.ban({ reason: `${modal.getTextInputValue("ban-reason") ?? "No reason given"}` });
        try {
            user.send({
                embeds: [
                    new EmbedBuilder()
                        .setDescription(`🚫 **BAN** \n U are banned from ${modal.guild.name} by ${modal.member.user.username} \nReason: ${modal.getTextInputValue("ban-reason") ?? "No reason given"}`)
                        .setColor("Red")
                ]
            });
        } catch (error) {
            console.log(error);
        }

        modal.reply({
            embeds: [
                new EmbedBuilder()
                    .setDescription(`🚫 <@${modal.user.id}> banned user \`${user.user.username}\`\nReason: ${modal.getTextInputValue("ban-reason") ?? "No reason given"}`)
                    .setColor("Red")
            ]
        });
    }
};