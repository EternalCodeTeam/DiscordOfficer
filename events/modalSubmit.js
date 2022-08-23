module.exports = (client) => {
    client.on('modalSubmit', async (modal) => {
        await require('./interactions/modals/modalInteraction.js').banModal(modal, client);
    })
}