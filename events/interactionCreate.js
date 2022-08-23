module.exports = (client) => {
    client.on('interactionCreate', async interaction => {
        await require('./interactions/slashCommand.js')(client, interaction)
    });
}