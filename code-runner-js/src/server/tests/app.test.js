/**
 * @jest-environment node
 */

const express = require('express');
// let request = require('supertest');
// const session = require('supertest-session');
const axios = require('axios');

axios.defaults.withCredentials = true;

const app = require('../app');

describe('test express routing', () => {
  let server;
  let testSession;

  beforeAll(() => {
    server = app.listen(6000);
  });

  describe('test authenticated', () => {
    test('should return index page if authenticated', async () => {
      await axios.post('http://localhost:6000/api/login', { email: 'test@epam.com' })
        .then((res) => {
          console.log(res);
        });
      await axios
        .get('http://localhost:6000/')
        .then((res) => {
          console.log(res);
          expect(res.status).toEqual(200);
        });
    });

    test('should return index page if authenticated', async () => {
      await axios.post('http://localhost:6000/api/login', { email: 'test@epam.com' })
        .then((res) => {
          console.log(res);
        });
      await axios
        .get('http://localhost:6000/login')
        .then((res) => {
          console.log(res);
          expect(res.status).toEqual(302);
        });
    });
  });

  describe('not authenticated', () => {
    test('should redirect if not authenticated', (done) => {
      testSession
        .get('/index')
        .expect(302, (err, res) => {
          if (err) { return done(err); }
          const redirectUrl = res.headers.location;
          expect(redirectUrl).toEqual('/login');
          done();
        });
    });

    test('should redirect if not authenticated', (done) => {
      testSession
        .get('/api/tasks')
        .expect(302, (err, res) => {
          if (err) { return done(err); }
          const redirectUrl = res.headers.location;
          expect(redirectUrl).toEqual('/login');
          done();
        });
    });
  });

  afterAll(() => {
    server.close();
  });
});
