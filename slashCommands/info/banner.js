const {
    ApplicationCommandType,
    EmbedBuilder,
    ActionRowBuilder,
    ButtonBuilder,
    ApplicationCommandOptionType
} = require("discord.js");
const { getBannerUrl } = require("../../utils/UserUtil");

module.exports = {
    name: "banner",
    description: "Display user's banner",
    type: ApplicationCommandType.ChatInput,
    cooldown: 3000,
    options: [
        {
            name: "user",
            description: "The banner of the user you want to display.",
            type: ApplicationCommandOptionType.User
        }
    ],
    run: async (client, interaction) => {
        const user = interaction.options.get("user")?.user || interaction.user;
        const image = await getBannerUrl(user.id, { size: 4096, dynamic: true });

        const embed = new EmbedBuilder()
            .setTitle(`🖼️ ${user.tag}'s banner`)
            .setImage(image)
            .setColor("#2f3136")
            .setTimestamp();

        return interaction.reply({ embeds: [embed] });
    }
};