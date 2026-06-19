const express = require('express');
const cors = require('cors');

const customerRoutes = require('./src/routes/customerRoutes');
const locationRoutes = require('./src/routes/locationRoutes');
const categoryRoutes = require('./src/routes/categoryRoutes');

const app = express();

app.use(cors());
app.use(express.json());

app.get('/', (req, res) => {
  res.json({
    message: 'Mobile Data Capture Backend API is running',
  });
});

app.get('/health', (req, res) => {
  res.json({
    status: 'OK',
    timestamp: new Date().toISOString(),
  });
});

app.use('/api/customers', customerRoutes);
app.use('/api/locations', locationRoutes);
app.use('/api/categories', categoryRoutes);

module.exports = app;