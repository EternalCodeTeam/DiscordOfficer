const { Events } = require("discord.js");
const { ActivityType } = require("discord-api-types/v10");

module.exports = {
    name: Events.ClientReady,
    once: true,
    async execute(client) {
        const activities = [
            { name: `${client.guilds.cache.size} Servers`, type: ActivityType.Listening },
            { name: `${client.channels.cache.size} Channels`, type: ActivityType.Playing },
            { name: `${client.users.cache.size} Users`, type: ActivityType.Watching },
            { name: `Discord.js v14`, type: ActivityType.Competing }
        ];

        const status = [
            "online",
            "dnd",
            "idle"
        ];

        let i = 0;
        setInterval(() => {
            if (i >= activities.length) {
                i = 0;
            }
            client.user.setActivity(activities[i]);
            i++;
        }, 5000);

        let s = 0;
        setInterval(() => {
            if (s >= activities.length) {
                s = 0;
            }
            client.user.setStatus(status[s]);
            s++;
        }, 30000);

        logger.info(`Logged in as ${client.user.tag}`);
    }
};
