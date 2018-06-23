const redis = require('redis');
const task1 = require('./task1');


const client = redis.createClient(); // this creates a new client
client.on('connect', () => {
  console.log('Redis client connected');
});
client.on('error', (err) => {
  console.log(`Something went wrong ${err}`);
});

client.set('task:1', JSON.stringify(task1));

client.get('task:1', (err, reply) => console.log(reply));
