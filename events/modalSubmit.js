module.exports = (client) => {
    client.on("modalSubmit", async (modal) => {
        const modalInit = require("./interactions/modals/modalInteraction.js");
        await modalInit.banModal(modal, client);
        await modalInit.kickModal(modal);
    });
};