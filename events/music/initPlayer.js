const { Player } = require("discord-player");

module.exports = async (client) => {
    global.eternalPlayer = new Player(client);
};