const { EmbedBuilder } = require("discord.js");
const { SlashCommandBuilder } = require("@discordjs/builders");
const { ChannelType, PermissionFlagsBits } = require("discord-api-types/v10");

module.exports = {
    data: new SlashCommandBuilder()
        .setName("embed")
        .setDescription("create quickly embed for specific channel")
        .addStringOption(option => option.setName("title").setDescription("The title").setRequired(true))
        .addStringOption(option => option.setName("description").setDescription("The description").setRequired(true))
        .addChannelOption(option => option.addChannelTypes(ChannelType.GuildText).setName("channel").setDescription("The channel").setRequired(false))
        .addStringOption(option => option.setName("color").setDescription("The color").setRequired(false))
        .addStringOption(option => option.setName("footer").setDescription("The footer").setRequired(false))
        .addStringOption(option => option.setName("thumbnail").setDescription("The thumbnail").setRequired(false))
        .addStringOption(option => option.setName("image").setDescription("The image").setRequired(false))
        .setDefaultMemberPermissions(PermissionFlagsBits.Administrator | PermissionFlagsBits.ManageMessages),
    execute: async (interaction) => {
        const title = interaction.options.getString("title");
        const description = interaction.options.getString("description");
        const color = interaction.options.getString("color") || "#2f3136";
        const footer = interaction.options.getString("footer") || `Requested by ${interaction.user.tag}`;
        const thumbnail = interaction.options.getString("thumbnail") || null;
        const image = interaction.options.getString("image") || null;

        const channel = interaction.options.getChannel("channel") || interaction.channel;

        try {
            const embed = new EmbedBuilder()
                .setTitle(title.toString())
                .setDescription(description.toString())
                .setColor(color.toString())
                .setThumbnail(thumbnail)
                .setImage(image)
                .setFooter({ text: footer.toString(), iconURL: interaction.user.displayAvatarURL({ dynamic: true, size: 4096 }) });

            interaction.reply({ content: `Embed created in ${channel.toString()}`, ephemeral: true });
            channel.send({ embeds: [embed] });
        } catch (error) {
            interaction.reply({ content: "Something went wrong!", ephemeral: true });
        }
    }
};