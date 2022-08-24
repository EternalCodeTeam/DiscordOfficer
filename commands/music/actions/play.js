const {Modal, TextInputComponent, showModal} = require("discord-modals");
module.exports = async (interaction, client) => {
    const playMusicModal = new Modal()
        .setCustomId('play-music-modal')
        .setTitle('Play your favorite music')
        .addComponents(
            new TextInputComponent()
                .setCustomId('music-url')
                .setStyle('SHORT')
                .setLabel("Insert here your music URL")
                .setRequired(true),
        )

    await showModal(playMusicModal, {
        client,
        interaction
    })
}
