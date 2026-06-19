const express = require('express');
const multer = require('multer');
const fs = require('fs');
const path = require('path');

const pool = require('../config/db');

const router = express.Router();

const uploadFolder = path.join(__dirname, '../../uploads/captured-images');

if (!fs.existsSync(uploadFolder)) {
  fs.mkdirSync(uploadFolder, { recursive: true });
}

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, uploadFolder);
  },
  filename: (req, file, cb) => {
    const uniqueName = `${Date.now()}-${Math.round(
      Math.random() * 1e9
    )}${path.extname(file.originalname)}`;

    cb(null, uniqueName);
  },
});

const upload = multer({ storage });

function buildImageUrl(req, imageUrl) {
  if (!imageUrl) {
    return null;
  }

  return `${req.protocol}://${req.get('host')}${imageUrl}`;
}

router.get('/', async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT
        cr.id,
        cr.customer_id,
        cr.location_id,
        cr.category_id,
        c.name AS customer_name,
        l.name AS location_name,
        cat.name AS category_name,
        cr.description,
        cr.latitude,
        cr.longitude,
        cr.image_url,
        cr.image_path,
        cr.captured_at,
        cr.received_at,
        cr.created_at,
        cr.updated_at
      FROM captured_records cr
      LEFT JOIN customers c ON c.id = cr.customer_id
      LEFT JOIN locations l ON l.id = cr.location_id
      LEFT JOIN categories cat ON cat.id = cr.category_id
      ORDER BY cr.received_at DESC
    `);

    const records = result.rows.map((record) => ({
      ...record,
      full_image_url: buildImageUrl(req, record.image_url),
    }));

    res.json(records);
  } catch (error) {
    res.status(500).json({
      message: 'Failed to fetch captured records',
      error: error.message,
    });
  }
});

router.get('/:id', async (req, res) => {
  try {
    const { id } = req.params;

    const result = await pool.query(
      `
      SELECT
        cr.id,
        cr.customer_id,
        cr.location_id,
        cr.category_id,
        c.name AS customer_name,
        l.name AS location_name,
        cat.name AS category_name,
        cr.description,
        cr.latitude,
        cr.longitude,
        cr.image_url,
        cr.image_path,
        cr.captured_at,
        cr.received_at,
        cr.created_at,
        cr.updated_at
      FROM captured_records cr
      LEFT JOIN customers c ON c.id = cr.customer_id
      LEFT JOIN locations l ON l.id = cr.location_id
      LEFT JOIN categories cat ON cat.id = cr.category_id
      WHERE cr.id = $1
      LIMIT 1
      `,
      [id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        message: 'Captured record not found',
      });
    }

    const record = result.rows[0];

    res.json({
      ...record,
      full_image_url: buildImageUrl(req, record.image_url),
    });
  } catch (error) {
    res.status(500).json({
      message: 'Failed to fetch captured record',
      error: error.message,
    });
  }
});

router.post('/', upload.single('image'), async (req, res) => {
  const client = await pool.connect();

  try {
    const {
      customer_id,
      location_id,
      category_id,
      description,
      latitude,
      longitude,
      captured_at,
    } = req.body;

    if (!customer_id || !location_id || !category_id) {
      return res.status(400).json({
        message: 'Customer, location and category are required',
      });
    }

    const imageUrl = req.file
      ? `/uploads/captured-images/${req.file.filename}`
      : null;

    const imagePath = req.file ? req.file.path.replace(/\\/g, '/') : null;

    await client.query('BEGIN');

    const recordResult = await client.query(
      `
      INSERT INTO captured_records (
        customer_id,
        location_id,
        category_id,
        description,
        latitude,
        longitude,
        image_url,
        image_path,
        captured_at
      )
      VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
      RETURNING *
      `,
      [
        customer_id,
        location_id,
        category_id,
        description || '',
        latitude || null,
        longitude || null,
        imageUrl,
        imagePath,
        captured_at || new Date().toISOString(),
      ]
    );

    const capturedRecord = recordResult.rows[0];

    if (req.file) {
      await client.query(
        `
        INSERT INTO captured_images (
          captured_record_id,
          image_url,
          storage_path
        )
        VALUES ($1, $2, $3)
        `,
        [capturedRecord.id, imageUrl, imagePath]
      );
    }

    await client.query('COMMIT');

    res.status(201).json({
      message: 'Captured record created successfully',
      record: {
        ...capturedRecord,
        full_image_url: buildImageUrl(req, capturedRecord.image_url),
      },
    });
  } catch (error) {
    await client.query('ROLLBACK');

    res.status(500).json({
      message: 'Failed to create captured record',
      error: error.message,
    });
  } finally {
    client.release();
  }
});

module.exports = router;