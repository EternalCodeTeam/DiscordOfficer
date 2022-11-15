const axios = require("axios");
const client = require("..");

module.exports = {
    getBannerUrl: async function (userId, { dynamicFormat = true, defaultFormat = "webp", size = 1024 } = {}) {

        if (![16, 32, 64, 128, 256, 512, 1024, 2048, 4096].includes(size)) {
            throw new Error(`The size '${size}' is not supported!`);
        }

        if (!["webp", "png", "jpg", "jpeg"].includes(defaultFormat)) {
            throw new Error(`The format '${defaultFormat}' is not supported as a default format!`);
        }

        const user = await client.users.fetch(userId, { force: true});

        if (!user.banner) {
            return null;
        }

        const query = `?size=${size}`;
        const baseUrl = `https://cdn.discordapp.com/banners/${userId}/${user.banner}`;

        if (dynamicFormat) {
            const { headers } = await axios.head(baseUrl);
            if (headers && headers.hasOwnProperty("content-type")) {
                return baseUrl + (headers["content-type"] === "image/gif" ? ".gif" : `.${defaultFormat}`) + query;
            }
        }

        return baseUrl + `.${defaultFormat}` + query;
    }
};