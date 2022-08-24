const modalInit = require('./interactions/modals/modalInteraction.js');
module.exports = (client) => {
    client.on("modalSubmit", async (modal) => {
        const modalInit = require("./interactions/modals/modalInteraction.js");
        await modalInit.administration.banModal(modal, client);
        await modalInit.administration.kickModal(modal);
        await modalInit.music.play(modal);
        await modalInit.music.search(modal);
    })
}
