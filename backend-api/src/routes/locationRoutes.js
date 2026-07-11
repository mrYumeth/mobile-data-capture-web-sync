const express = require('express');
const pool = require('../config/db');

const router = express.Router();

function requireAdmin(req, res) {
  if (req.user?.role !== 'admin') {
    res.status(403).json({
      message: 'Admin access is required',
    });
    return false;
  }

  return true;
}

router.get('/', async (req, res) => {
  try {
    const result = await pool.query(
    `
    SELECT *
    FROM locations
    WHERE tenant_id = $1
      AND is_active = TRUE
    ORDER BY id DESC
    `,
    [req.user.tenantId]
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
  if (!requireAdmin(req, res)) {
    return;
  }

  try {
    const { name, address } = req.body;

    if (!name) {
      return res.status(400).json({
        message: 'Location name is required',
      });
    }

    const result = await pool.query(
    `
    INSERT INTO locations (tenant_id, name, address)
    VALUES ($1, $2, $3)
    RETURNING *
    `,
    [req.user.tenantId, name, address]
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
  if (!requireAdmin(req, res)) {
    return;
  }

  try {
    const { id } = req.params;
    const { name, address } = req.body;

    const result = await pool.query(
      `
      UPDATE locations
      SET name = $1,
          address = $2,
          updated_at = CURRENT_TIMESTAMP
      WHERE id = $3
        AND tenant_id = $4
      RETURNING *
      `,
      [name, address, id, req.user.tenantId]
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
  if (!requireAdmin(req, res)) {
    return;
  }

  try {
    const { id } = req.params;

    const result = await pool.query(
      `
      DELETE FROM locations
      WHERE id = $1
        AND tenant_id = $2
      RETURNING *
      `,
      [id, req.user.tenantId]
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