module.exports = (req, res, next) => {
  console.log(req.session);
  if (req.session.userId) {
    return next();
  }
  console.log('Not Authenticated');
  res.redirect('/login');
};
