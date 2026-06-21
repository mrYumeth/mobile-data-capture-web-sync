const express = require('express');
const pool = require('../config/db');

const router = express.Router();

router.get('/', async (req, res) => {
  try {
    const result = await pool.query(
      'SELECT * FROM locations WHERE is_active = TRUE ORDER BY id DESC'
    );

    res.json(result.rows);
  } catch (error) {
    res.status(500).json({
      message: 'Failed to fetch locations',
      error: error.message,
    });
  }
});

router.post('/', async (req, res) => {
  try {
    const { name, address } = req.body;

    if (!name) {
      return res.status(400).json({
        message: 'Location name is required',
      });
    }

    const result = await pool.query(
      `INSERT INTO locations (name, address)
       VALUES ($1, $2)
       RETURNING *`,
      [name, address]
    );

    res.status(201).json(result.rows[0]);
  } catch (error) {
    res.status(500).json({
      message: 'Failed to create location',
      error: error.message,
    });
  }
});

router.put('/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const { name, address } = req.body;

    const result = await pool.query(
      `UPDATE locations
       SET name = $1,
           address = $2,
           updated_at = CURRENT_TIMESTAMP
       WHERE id = $3
       RETURNING *`,
      [name, address, id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        message: 'Location not found',
      });
    }

    res.json(result.rows[0]);
  } catch (error) {
    res.status(500).json({
      message: 'Failed to update location',
      error: error.message,
    });
  }
});

router.delete('/:id', async (req, res) => {
  try {
    const { id } = req.params;

    const result = await pool.query(
      'DELETE FROM locations WHERE id = $1 RETURNING *',
      [id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        message: 'Location not found',
      });
    }

    res.json({
      message: 'Location deleted successfully',
      deletedLocation: result.rows[0],
    });
  } catch (error) {
    res.status(500).json({
      message: 'Failed to delete location',
      error: error.message,
    });
  }
});

module.exports = router;