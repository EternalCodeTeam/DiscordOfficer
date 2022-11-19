const fs = require("fs");

module.exports = (client) => {
    try {
        fs.readdirSync("./events/").filter((file) => file.endsWith(".js")).forEach((file) => {
            const event = require(`../events/${file}`);

            if (!("name" in event && "once" in event && "execute" in event)) {
                logger.warn(`The command at ${file} is missing a required "data", "once" or "execute" property.`);
            }

            if (event.once) {
                client.once(event.name, (...args) => event.execute(...args));
            } else {
                client.on(event.name, (...args) => event.execute(...args));
            }

            delete require.cache[require.resolve(`../events/${file}`)];
        });

        logger.info("All events successfully loaded.");
    } catch (error) {
        logger.error(error);
    }
};