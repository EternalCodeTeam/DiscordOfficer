const {Modal, TextInputComponent, showModal} = require("discord-modals");
module.exports = async (interaction, client) => {
    const searchMusicModal = new Modal()
        .setCustomId('search-music-modal')
        .setTitle('Search your favorite song')
        .addComponents(
            new TextInputComponent()
                .setCustomId('music-title')
                .setStyle('SHORT')
                .setLabel("Type title of the song")
                .setRequired(true),
        )

    await showModal(searchMusicModal, {
        client,
        interaction
    });
}
