const { Player } = require("discord-player");

module.exports = async (client) => {
    global.eternalPlayer = new Player(client);
    global.eternalPlayerVolume = 100;

    eternalPlayer.on("trackStart", async (queue, track) => {
        queue.setVolume(eternalPlayerVolume);
    });
};
