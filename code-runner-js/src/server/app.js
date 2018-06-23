const express = require('express');
const fileUpload = require('express-fileupload');
const axios = require('axios');

const cookieParser = require('cookie-parser');
const bodyParser = require('body-parser');
const expressValidator = require('express-validator');
const session = require('express-session');

const app = express();
const path = require('path');
const isAuthenticated = require('./auth/isAuthenticated');

const tasksApi = require('./routes/tasks');
const solutionApi = require('./routes/solution');


app.use(fileUpload());

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(cookieParser());
app.use(session({
  secret: 'java-and-js-are-friends-forever:D',
  resave: false,
  saveUninitialized: true,
  // cookie: { secure: true }
}));

app.use(expressValidator());

app.use('/public', express.static(`${__dirname}./../../dist/public`));

app.use('/api', [tasksApi, solutionApi]);

app.post('/api/login', (req, res) => {
  if (req.body.email) {
    console.log('auth as ', req.body.email);
    req.session.userId = req.body.email;
    console.log(req.session);
    req.session.save();
    res.send(`Authenticated as ${req.body.email}`);
  } else {
    console.log('No email provided');
  }
});

app.get('/login', (req, res) => {
  res.sendFile(path.join(`${__dirname}./../../dist/public/index.html`));
  if (req.session.userId) {
    res.redirect('/');
  }
});

app.get('*', isAuthenticated, (req, res) => {
  console.log('Returning index html for all');
  res.sendFile(path.join(`${__dirname}./../../dist/public/index.html`));
});

module.exports = app;
