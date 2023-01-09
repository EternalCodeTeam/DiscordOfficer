FROM node:18-alpine


LABEL org.opencontainers.image.authors = "EternalCode.pl <mail@eternalcode.pl>"
RUN adduser --disabled-password --home /home/eternalcode eternalcode
WORKDIR /home/eternalcode/
USER eternalcode
ENV USER=eternalcode HOME=/home/eternalcode
COPY . /home/eternalcode/
WORKDIR /home/eternalcode
RUN cp .env.example .env

RUN npm i
CMD [ "node", "index.js" ]