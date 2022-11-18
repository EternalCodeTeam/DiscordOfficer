const moment = require("moment");
const chalk = require("chalk");
const util = require("util");

module.exports = class Logger {
    static info(content, { color = "blue", tag = " INFO " } = {}) {
        this.write(content, { color, tag });
    }

    static debug(content, { color = "blue", tag = " DEBUG " } = {}) {
        this.write(content, { color, tag });
    }

    static warn(content, { color = "yellow", tag = " WARN " } = {}) {
        this.write(content, { color, tag });
    }

    static error(content, { color = "red", tag = " ERROR " } = {}) {
        this.write(content, { color, tag, error: true });
    }

    static write(content, { color = "grey", tag = " INFO ", error = false } = {}) {
        const timestamp = chalk.cyan(`[${moment().format("DD-MM-YYYY kk:mm:ss")}]:`);

        const levelTag = chalk.bold(`[${tag}]`);
        const text = chalk[color](this.clean(content));
        const stream = error ? process.stderr : process.stdout;

        stream.write(`${timestamp} ${levelTag} ${text}\n`);
    }

    static clean(item) {
        if (typeof item === "string") {
            return item;
        }

        return util.inspect(item, { depth: Infinity });
    }
};