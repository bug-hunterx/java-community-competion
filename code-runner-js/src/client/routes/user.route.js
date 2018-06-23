const express = require('express');

const router = express.Router();

router.get('/', (req, res) => {
  res.render('add', { errors: req.session.errors });
  req.session.errors = null;
});

module.exports = router;
