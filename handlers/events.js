const fs = require("fs");

module.exports = (client) => {
    try {
        fs.readdirSync("./events/").filter((file) => file.endsWith(".js")).forEach((file) => {
            const filePath = `../events/${file}`;
            const event = require(filePath);

            if (event.once) {
                client.once(event.name, (...args) => event.execute(...args));
            } else {
                client.on(event.name, (...args) => event.execute(...args));
            }

            delete require.cache[require.resolve(filePath)];
        });

        logger.info("All events successfully loaded.");
    } catch (error) {
        logger.error(error);
    }
};