const express = require('express');
const pool = require('../config/db');

const router = express.Router();

router.get('/', async (req, res) => {
  try {
    const result = await pool.query(
      'SELECT * FROM categories WHERE is_active = TRUE ORDER BY id DESC'
    );

    res.json(result.rows);
  } catch (error) {
    res.status(500).json({
      message: 'Failed to fetch categories',
      error: error.message,
    });
  }
});

router.post('/', async (req, res) => {
  try {
    const { name, description } = req.body;

    if (!name) {
      return res.status(400).json({
        message: 'Category name is required',
      });
    }

    const result = await pool.query(
      `INSERT INTO categories (name, description)
       VALUES ($1, $2)
       RETURNING *`,
      [name, description]
    );

    res.status(201).json(result.rows[0]);
  } catch (error) {
    res.status(500).json({
      message: 'Failed to create category',
      error: error.message,
    });
  }
});

router.put('/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const { name, description } = req.body;

    const result = await pool.query(
      `UPDATE categories
       SET name = $1,
           description = $2,
           updated_at = CURRENT_TIMESTAMP
       WHERE id = $3
       RETURNING *`,
      [name, description, id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        message: 'Category not found',
      });
    }

    res.json(result.rows[0]);
  } catch (error) {
    res.status(500).json({
      message: 'Failed to update category',
      error: error.message,
    });
  }
});

router.delete('/:id', async (req, res) => {
  try {
    const { id } = req.params;

    const result = await pool.query(
      'DELETE FROM categories WHERE id = $1 RETURNING *',
      [id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        message: 'Category not found',
      });
    }

    res.json({
      message: 'Category deleted successfully',
      deletedCategory: result.rows[0],
    });
  } catch (error) {
    res.status(500).json({
      message: 'Failed to delete category',
      error: error.message,
    });
  }
});

module.exports = router;