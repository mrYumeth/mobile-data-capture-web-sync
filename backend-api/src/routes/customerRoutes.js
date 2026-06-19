const express = require('express');
const pool = require('../config/db');

const router = express.Router();

function isValidPhoneNumber(phone) {
  if (!phone) {
    return true;
  }

  return /^\d{10}$/.test(phone);
}

router.get('/', async (req, res) => {
  try {
    const result = await pool.query(
      'SELECT * FROM customers ORDER BY id DESC'
    );

    res.json(result.rows);
  } catch (error) {
    res.status(500).json({
      message: 'Failed to fetch customers',
      error: error.message,
    });
  }
});

router.post('/', async (req, res) => {
  try {
    const { name, phone, email, address } = req.body;

    if (!name || !name.trim()) {
      return res.status(400).json({
        message: 'Customer name is required',
      });
    }

    if (!isValidPhoneNumber(phone)) {
      return res.status(400).json({
        message: 'Phone number must contain exactly 10 digits',
      });
    }

    const result = await pool.query(
      `INSERT INTO customers (name, phone, email, address)
       VALUES ($1, $2, $3, $4)
       RETURNING *`,
      [name.trim(), phone || null, email || null, address || null]
    );

    res.status(201).json(result.rows[0]);
  } catch (error) {
    res.status(500).json({
      message: 'Failed to create customer',
      error: error.message,
    });
  }
});

router.put('/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const { name, phone, email, address } = req.body;

    if (!name || !name.trim()) {
      return res.status(400).json({
        message: 'Customer name is required',
      });
    }

    if (!isValidPhoneNumber(phone)) {
      return res.status(400).json({
        message: 'Phone number must contain exactly 10 digits',
      });
    }

    const result = await pool.query(
      `UPDATE customers
       SET name = $1,
           phone = $2,
           email = $3,
           address = $4,
           updated_at = CURRENT_TIMESTAMP
       WHERE id = $5
       RETURNING *`,
      [name.trim(), phone || null, email || null, address || null, id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        message: 'Customer not found',
      });
    }

    res.json(result.rows[0]);
  } catch (error) {
    res.status(500).json({
      message: 'Failed to update customer',
      error: error.message,
    });
  }
});

router.delete('/:id', async (req, res) => {
  try {
    const { id } = req.params;

    const result = await pool.query(
      'DELETE FROM customers WHERE id = $1 RETURNING *',
      [id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        message: 'Customer not found',
      });
    }

    res.json({
      message: 'Customer deleted successfully',
      deletedCustomer: result.rows[0],
    });
  } catch (error) {
    res.status(500).json({
      message: 'Failed to delete customer',
      error: error.message,
    });
  }
});

module.exports = router;