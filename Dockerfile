FROM node:12-alpine
RUN apk add --no-cache python3 g++ make
WORKDIR /app
COPY project .
RUN yarn install --production
CMD ["node", "src/index.js"]