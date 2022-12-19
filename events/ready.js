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

        const statuses = [
            "online",
            "dnd",
            "idle"
        ];

        let activityIndex = 0;
        setInterval(() => {
            if (activityIndex >= activities.length) {
                activityIndex = 0;
            }

            client.user.setActivity(activities[activityIndex]);
            activityIndex++;
        }, 5000);

        let statusIndex = 0;
        setInterval(() => {
            if (statusIndex >= statuses.length) {
                statusIndex = 0;
            }

            client.user.setStatus(statuses[statusIndex]);
            statusIndex++;
        }, 30000);

        logger.info(`Logged in as ${client.user.tag}`);
    }
};
